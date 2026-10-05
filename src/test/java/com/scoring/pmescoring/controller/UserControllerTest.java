package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.dto.request.user.UpdateUserRequest;
import com.scoring.pmescoring.dto.request.user.UserFilter;
import com.scoring.pmescoring.dto.request.user.UserLogin;
import com.scoring.pmescoring.dto.request.user.UserRequest;
import com.scoring.pmescoring.dto.response.user.DataTokenResponse;
import com.scoring.pmescoring.dto.response.user.UserResponse;
import com.scoring.pmescoring.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = ControllerTestSupport.standalone(controller);
    }

    @AfterEach
    void tearDown() {
        ControllerTestSupport.clearCurrentRequest();
    }

    @Nested
    @DisplayName("Cadastro e consulta")
    class Crud {

        @Test
        @DisplayName("POST cadastra o usuário e devolve 201 com o header Location")
        void registerShouldReturnCreatedWithLocation() {
            ControllerTestSupport.bindCurrentRequest("POST", BASE_URL);
            UserRequest request = mock(UserRequest.class);
            UserResponse response = mock(UserResponse.class);
            doReturn(3L).when(response).id();
            when(userService.create(request)).thenReturn(response);

            ResponseEntity<UserResponse> result = controller.register(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getHeaders().getLocation()).hasToString("http://localhost/api/v1/users/3");
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("POST propaga a BusinessException de e-mail duplicado")
        void registerShouldPropagateDuplicateEmail() {
            UserRequest request = mock(UserRequest.class);
            when(userService.create(request)).thenThrow(new BusinessException("User already exists with this email."));

            assertThatThrownBy(() -> controller.register(request)).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("GET /{id} devolve 200 com o usuário")
        void getShouldReturnOk() {
            UserResponse response = mock(UserResponse.class);
            when(userService.findById(3L)).thenReturn(response);

            ResponseEntity<UserResponse> result = controller.getUser(3L);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("A rota funciona mesmo com o @RequestMapping sem barra inicial (\"api/v1/users\")")
        void listRouteShouldWorkAndUseDefaultPageable() throws Exception {
            when(userService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

            mvc.perform(get(BASE_URL)).andExpect(status().isOk());

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(userService).findAll(captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
            assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
        }

        @Test
        @DisplayName("GET /filter repassa o filtro e a paginação para o service")
        void filterShouldDelegate() {
            UserFilter filter = mock(UserFilter.class);
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserResponse> page = ControllerTestSupport.emptyPage();
            when(userService.findAllByFilter(filter, pageable)).thenReturn(page);

            ResponseEntity<Page<UserResponse>> result = controller.filterUsers(filter, pageable);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(page);
        }

        @Test
        @DisplayName("GET /filter propaga a BusinessException de intervalo de datas inválido")
        void filterShouldPropagateInvalidRange() {
            UserFilter filter = mock(UserFilter.class);
            Pageable pageable = PageRequest.of(0, 10);
            when(userService.findAllByFilter(filter, pageable))
                    .thenThrow(new BusinessException("The creation start date cannot be later than the end date."));

            assertThatThrownBy(() -> controller.filterUsers(filter, pageable)).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("PATCH /{id} atualiza e devolve 200")
        void updateShouldReturnOk() {
            UpdateUserRequest request = mock(UpdateUserRequest.class);
            UserResponse response = mock(UserResponse.class);
            when(userService.update(3L, request)).thenReturn(response);

            ResponseEntity<UserResponse> result = controller.update(3L, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("DELETE /{id} devolve 204")
        void deleteShouldReturnNoContent() throws Exception {
            mvc.perform(delete(BASE_URL + "/3")).andExpect(status().isNoContent());

            verify(userService).delete(3L);
        }

        @Test
        @DisplayName("DELETE /{id} propaga a BusinessException ao tentar excluir o admin")
        void deleteShouldPropagateAdminProtection() {
            doThrow(new BusinessException("Cannot delete admin user with ID: 1")).when(userService).delete(1L);

            assertThatThrownBy(() -> controller.delete(1L)).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("DELETE /{id} propaga ResourceNotFoundException para usuário inexistente")
        void deleteShouldPropagateNotFound() {
            doThrow(new ResourceNotFoundException("User not found with ID: 99")).when(userService).delete(99L);

            assertThatThrownBy(() -> controller.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Autenticação")
    class Authentication {

        @Test
        @DisplayName("POST /login devolve 200 com o token JWT")
        void loginShouldReturnToken() {
            UserLogin login = mock(UserLogin.class);
            DataTokenResponse token = new DataTokenResponse("jwt-token");
            when(userService.login(login)).thenReturn(token);

            ResponseEntity<DataTokenResponse> result = controller.login(login);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(token);
        }

        @Test
        @DisplayName("POST /login propaga a falha de credenciais")
        void loginShouldPropagateBadCredentials() {
            UserLogin login = mock(UserLogin.class);
            when(userService.login(login)).thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> controller.login(login)).isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("GET /valid/{token} recebe o JWT inteiro, inclusive os pontos, e devolve 200")
        void validationRouteShouldReceiveFullToken() throws Exception {
            String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0ZSJ9.assinatura";

            mvc.perform(get(BASE_URL + "/valid/" + jwt)).andExpect(status().isOk());

            verify(userService).validToken(jwt);
        }

        @Test
        @DisplayName("GET /valid/{token} propaga a exceção de token inválido")
        void validationShouldPropagateInvalidToken() {
            doThrow(new RuntimeException("Invalid or expired JWT token")).when(userService).validToken("invalido");

            assertThatThrownBy(() -> controller.validation("invalido")).isInstanceOf(RuntimeException.class);
        }
    }
}
