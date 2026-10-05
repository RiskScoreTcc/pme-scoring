package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.config.security.TokenService;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.mapper.UserMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String ADMIN_EMAIL = "admin@riskscore.com";
    private static final String EMAIL = "analista@riskscore.com";
    private static final List<EntityStatus> ACTIVE_OR_INACTIVE = List.of(EntityStatus.ACTIVE, EntityStatus.INACTIVE);

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PageableSanitizer pageableSanitizer;
    @Mock private AuthenticationManager manager;
    @Mock private TokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "adminEmail", ADMIN_EMAIL);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Rejeita e-mail já cadastrado em usuário ativo ou inativo")
        void shouldRejectDuplicateEmail() {
            UserRequest request = mock(UserRequest.class);
            when(request.email()).thenReturn(EMAIL);
            when(userRepository.existsByEmailAndStatusIn(EMAIL, ACTIVE_OR_INACTIVE)).thenReturn(true);

            assertThatThrownBy(() -> service.create(request)).isInstanceOf(BusinessException.class);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Salva a senha criptografada, nunca em texto puro")
        void shouldEncodePassword() {
            UserRequest request = mock(UserRequest.class);
            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn("senha123");
            when(userRepository.existsByEmailAndStatusIn(EMAIL, ACTIVE_OR_INACTIVE)).thenReturn(false);

            User user = mock(User.class);
            when(userMapper.toEntity(request)).thenReturn(user);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
            when(userRepository.save(user)).thenReturn(user);

            service.create(request);

            verify(user).setPassword("$2a$hash");
            verify(user, never()).setPassword("senha123");
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Não permite excluir o usuário administrador")
        void shouldNotDeleteAdmin() {
            User admin = mock(User.class);
            when(admin.getEmail()).thenReturn(ADMIN_EMAIL);
            when(userRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(Optional.of(admin));

            assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessException.class);
            verify(admin, never()).delete();
        }

        @Test
        @DisplayName("Exclui logicamente um usuário comum")
        void shouldDeleteRegularUser() {
            User user = mock(User.class);
            when(user.getEmail()).thenReturn(EMAIL);
            when(userRepository.findByIdAndStatus(2L, EntityStatus.ACTIVE)).thenReturn(Optional.of(user));

            service.delete(2L);

            verify(user).delete();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para usuário inexistente")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByIdAndStatus(99L, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Não permite desativar o administrador")
        void shouldNotDeactivateAdmin() {
            User admin = mock(User.class);
            when(admin.getEmail()).thenReturn(ADMIN_EMAIL);
            when(userRepository.findByIdAndStatusIn(1L, ACTIVE_OR_INACTIVE)).thenReturn(Optional.of(admin));

            UpdateUserRequest request = mock(UpdateUserRequest.class);
            when(request.isDeactivate()).thenReturn(true);

            assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(BusinessException.class);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Rejeita troca para e-mail já usado por outro usuário")
        void shouldRejectEmailInUse() {
            User user = mock(User.class);
            when(user.getEmail()).thenReturn(EMAIL);
            when(userRepository.findByIdAndStatusIn(2L, ACTIVE_OR_INACTIVE)).thenReturn(Optional.of(user));

            UpdateUserRequest request = mock(UpdateUserRequest.class);
            when(request.email()).thenReturn("outro@riskscore.com");
            when(userRepository.existsByEmailAndStatusIn("outro@riskscore.com", ACTIVE_OR_INACTIVE)).thenReturn(true);

            assertThatThrownBy(() -> service.update(2L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already in use");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Criptografa a nova senha quando ela é informada")
        void shouldEncodeNewPassword() {
            User user = mock(User.class);
            when(user.getEmail()).thenReturn(EMAIL);
            when(userRepository.findByIdAndStatusIn(2L, ACTIVE_OR_INACTIVE)).thenReturn(Optional.of(user));

            UpdateUserRequest request = mock(UpdateUserRequest.class);
            when(request.email()).thenReturn(EMAIL);
            when(request.password()).thenReturn("novaSenha");
            when(passwordEncoder.encode("novaSenha")).thenReturn("$2a$novoHash");
            when(userRepository.save(user)).thenReturn(user);

            service.update(2L, request);

            verify(user).setPassword("$2a$novoHash");
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Autentica, registra o último acesso e devolve o token JWT")
        void shouldReturnTokenAndUpdateLastAccess() {
            UserLogin login = mock(UserLogin.class);
            when(login.email()).thenReturn(EMAIL);
            when(login.password()).thenReturn("senha123");

            User user = mock(User.class);
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(user);
            when(manager.authenticate(any())).thenReturn(authentication);
            when(tokenService.generateToken(user)).thenReturn("jwt-token");

            DataTokenResponse result = service.login(login);

            assertThat(result).isEqualTo(new DataTokenResponse("jwt-token"));
            verify(user).setLastAccess(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Propaga a falha de credenciais e não gera token")
        void shouldNotGenerateTokenOnBadCredentials() {
            UserLogin login = mock(UserLogin.class);
            when(login.email()).thenReturn(EMAIL);
            when(login.password()).thenReturn("errada");
            when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> service.login(login)).isInstanceOf(BadCredentialsException.class);
            verifyNoInteractions(tokenService);
        }
    }
}