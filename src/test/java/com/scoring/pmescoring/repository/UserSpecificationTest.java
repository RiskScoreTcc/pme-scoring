package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UserFilter;
import com.scoring.pmescoring.model.TypeUser;
import com.scoring.pmescoring.repository.specification.UserSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.scoring.pmescoring.model.EntityStatus.ACTIVE;
import static com.scoring.pmescoring.model.EntityStatus.INACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSpecificationTest extends RepositoryTestBase {

    @Autowired
    private UserRepository userRepository;

    private User maria;
    private User joao;
    private User admin;

    @BeforeEach
    void setUp() {
        maria = persistUserWithLastAccess("maria.analista@riskscore.com", TypeUser.CREDIT_ANALYST,
                LocalDateTime.of(2026, 10, 4, 23, 30));
        joao = persistUserWithLastAccess("joao.analista@riskscore.com", TypeUser.CREDIT_ANALYST,
                LocalDateTime.of(2026, 10, 5, 0, 0));
        admin = persistUser("admin@riskscore.com", TypeUser.ADMIN, ACTIVE);
        joao.setStatus(INACTIVE);
        em.persistAndFlush(joao);
    }

    private User persistUserWithLastAccess(String email, TypeUser type, LocalDateTime lastAccess) {
        User user = new User(email, "$2a$10$senhaJaCriptografada", type);
        user.setLastAccess(lastAccess);
        return em.persistAndFlush(user);
    }

    private UserFilter filter() {
        UserFilter filter = mock(UserFilter.class);
        when(filter.id()).thenReturn(null);
        return filter;
    }

    private List<User> search(UserFilter filter) {
        return userRepository.findAll(UserSpecification.filter(filter));
    }

    @Test
    @DisplayName("Filtro vazio devolve todos os usuários")
    void emptyFilterShouldReturnAll() {
        assertThat(search(filter())).containsExactlyInAnyOrder(maria, joao, admin);
    }

    @Test
    @DisplayName("Filtra por id")
    void shouldFilterById() {
        UserFilter filter = filter();
        when(filter.id()).thenReturn(admin.getId());

        assertThat(search(filter)).containsExactly(admin);
    }

    @Test
    @DisplayName("E-mail busca por trecho, sem diferenciar maiúsculas de minúsculas")
    void shouldFilterByPartialEmailIgnoringCase() {
        UserFilter filter = filter();
        when(filter.email()).thenReturn("ANALISTA");

        assertThat(search(filter)).containsExactlyInAnyOrder(maria, joao);
    }

    @Test
    @DisplayName("E-mail em branco é ignorado")
    void blankEmailShouldBeIgnored() {
        UserFilter filter = filter();
        when(filter.email()).thenReturn("   ");

        assertThat(search(filter)).hasSize(3);
    }

    @Test
    @DisplayName("Filtra por tipo de usuário")
    void shouldFilterByType() {
        UserFilter filter = filter();
        when(filter.type()).thenReturn(TypeUser.ADMIN);

        assertThat(search(filter)).containsExactly(admin);
    }

    @Test
    @DisplayName("Filtra por status")
    void shouldFilterByStatus() {
        UserFilter filter = filter();
        when(filter.status()).thenReturn(INACTIVE);

        assertThat(search(filter)).containsExactly(joao);
    }

    @Test
    @DisplayName("Último acesso 'até' inclui o dia inteiro informado (até 23:59)")
    void lastAccessToShouldIncludeWholeDay() {
        UserFilter filter = filter();
        when(filter.lastAccessTo()).thenReturn(LocalDate.of(2026, 10, 4));

        assertThat(search(filter)).containsExactly(maria);
    }

    @Test
    @DisplayName("Último acesso 'de' começa à meia-noite do dia informado")
    void lastAccessFromShouldStartAtMidnight() {
        UserFilter filter = filter();
        when(filter.lastAccessFrom()).thenReturn(LocalDate.of(2026, 10, 5));

        assertThat(search(filter)).containsExactly(joao);
    }

    @Test
    @DisplayName("Usuário que nunca acessou não aparece quando há filtro de último acesso")
    void usersWithoutLastAccessShouldBeExcluded() {
        UserFilter filter = filter();
        when(filter.lastAccessFrom()).thenReturn(LocalDate.of(2000, 1, 1));

        assertThat(search(filter)).doesNotContain(admin);
    }

    @Test
    @DisplayName("Filtra pela data de criação")
    void shouldFilterByCreationDate() {
        UserFilter createdToday = filter();
        when(createdToday.creationDateFrom()).thenReturn(LocalDate.now());
        UserFilter createdUntilYesterday = filter();
        when(createdUntilYesterday.creationDateTo()).thenReturn(LocalDate.now().minusDays(1));

        assertThat(search(createdToday)).hasSize(3);
        assertThat(search(createdUntilYesterday)).isEmpty();
    }

    @Test
    @DisplayName("Vários filtros juntos são combinados com E (todos precisam bater)")
    void shouldCombineFiltersWithAnd() {
        UserFilter filter = filter();
        when(filter.email()).thenReturn("analista");
        when(filter.status()).thenReturn(ACTIVE);

        assertThat(search(filter)).containsExactly(maria);
    }
}
