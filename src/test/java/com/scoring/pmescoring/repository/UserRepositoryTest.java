package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.scoring.pmescoring.model.EntityStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTestBase {

    private static final List<EntityStatus> ACTIVE_OR_INACTIVE = List.of(ACTIVE, INACTIVE);

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmailAndStatus (usada no login) só encontra usuários ativos")
    void findByEmailAndStatusShouldFindOnlyActive() {
        persistUser("ativo@riskscore.com", TypeUser.CREDIT_ANALYST, ACTIVE);
        persistUser("inativo@riskscore.com", TypeUser.CREDIT_ANALYST, INACTIVE);

        assertThat(userRepository.findByEmailAndStatus("ativo@riskscore.com", ACTIVE)).isPresent();
        assertThat(userRepository.findByEmailAndStatus("inativo@riskscore.com", ACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("Comportamento atual: a busca por e-mail diferencia maiúsculas de minúsculas")
    void emailLookupIsCaseSensitive() {
        persistUser("maria@riskscore.com", TypeUser.CREDIT_ANALYST, ACTIVE);

        assertThat(userRepository.findByEmailAndStatus("Maria@RiskScore.com", ACTIVE)).isEmpty();
        assertThat(userRepository.existsByEmailAndStatusIn("Maria@RiskScore.com", ACTIVE_OR_INACTIVE)).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndStatusIn: e-mail de usuário inativo continua ocupado; de usuário excluído fica livre")
    void existsByEmailShouldConsiderInactiveButNotDeleted() {
        persistUser("inativo@riskscore.com", TypeUser.CREDIT_ANALYST, INACTIVE);
        persistUser("excluido@riskscore.com", TypeUser.CREDIT_ANALYST, DELETED);

        assertThat(userRepository.existsByEmailAndStatusIn("inativo@riskscore.com", ACTIVE_OR_INACTIVE)).isTrue();
        assertThat(userRepository.existsByEmailAndStatusIn("excluido@riskscore.com", ACTIVE_OR_INACTIVE)).isFalse();
    }

    @Test
    @DisplayName("findByIdAndStatusIn (usada no update) encontra ativos e inativos, mas não excluídos")
    void findByIdAndStatusInShouldIgnoreDeleted() {
        User inactive = persistUser("inativo@riskscore.com", TypeUser.CREDIT_ANALYST, INACTIVE);
        User deleted = persistUser("excluido@riskscore.com", TypeUser.CREDIT_ANALYST, DELETED);

        assertThat(userRepository.findByIdAndStatusIn(inactive.getId(), ACTIVE_OR_INACTIVE)).contains(inactive);
        assertThat(userRepository.findByIdAndStatusIn(deleted.getId(), ACTIVE_OR_INACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndStatus não encontra usuário excluído ao buscar por ACTIVE")
    void findByIdAndStatusShouldRespectStatus() {
        User deleted = persistUser("excluido@riskscore.com", TypeUser.CREDIT_ANALYST, DELETED);

        assertThat(userRepository.findByIdAndStatus(deleted.getId(), ACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("existsByEmailAndStatus considera só o status pedido")
    void existsByEmailAndStatus() {
        persistUser("ativo@riskscore.com", TypeUser.ADMIN, ACTIVE);

        assertThat(userRepository.existsByEmailAndStatus("ativo@riskscore.com", ACTIVE)).isTrue();
        assertThat(userRepository.existsByEmailAndStatus("ativo@riskscore.com", INACTIVE)).isFalse();
    }

    @Test
    @DisplayName("findByStatus pagina só os usuários com o status pedido")
    void findByStatusShouldPaginate() {
        persistUser("a@riskscore.com", TypeUser.CREDIT_ANALYST, ACTIVE);
        persistUser("b@riskscore.com", TypeUser.CREDIT_ANALYST, ACTIVE);
        persistUser("c@riskscore.com", TypeUser.ADMIN, ACTIVE);
        persistUser("d@riskscore.com", TypeUser.CREDIT_ANALYST, DELETED);

        Page<User> firstPage = userRepository.findByStatus(ACTIVE, PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("@PrePersist preenche a data de criação")
    void shouldFillCreationDate() {
        User user = persistUser("a@riskscore.com", TypeUser.CREDIT_ANALYST, ACTIVE);

        assertThat(user.getCreationDate()).isNotNull();
    }
}
