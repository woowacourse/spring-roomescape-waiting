package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.User;
import roomescape.domain.UserRole;
import roomescape.domain.repository.UserRepository;

@Sql(scripts = {"/test-schema.sql"})
@JdbcTest
class UserJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserJdbcRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("유저를 아이디로 조회한다.")
    void findById() {
        // given
        var user = readyUser();
        var savedId = repository.save(user);

        // when
        Optional<User> found = repository.findById(savedId);

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("유저를 저장한다.")
    void save() {
        // given
        var user = readyUser();

        // when
        repository.save(user);

        // then
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("모든 유저를 조회한다.")
    void findAll() {
        // given
        var user1 = User.createUser("유저1", "abc@email.com", "password");
        var user2 = User.createUser("유저2", "def@email.com", "password");
        repository.save(user1);
        repository.save(user2);

        // when
        var users = repository.findAll();

        // then
        assertThat(users).hasSize(2);
    }

    private User readyUser() {
        return new User(1L, "유저", UserRole.USER, "email@email.com", "password");
    }
}
