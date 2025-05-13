package roomescape.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.User;
import roomescape.domain.repository.UserRepository;

@Repository
public class UserJdbcRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(final long id) {
        var sql = "select id, name, role, email, password from USERS where id = ?";
        var userList = jdbcTemplate.query(sql, RowMappers.USER, id);
        return userList.stream().findAny();
    }

    @Override
    public long save(final User user) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
        var generatedId = insert.withTableName("USERS")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "name", user.name(),
                "role", user.role().name(),
                "email", user.email(),
                "password", user.password()
            ));
        return generatedId.longValue();
    }

    @Override
    public List<User> findAll() {
        var sql = "select id, name, role, email, password from USERS";
        return jdbcTemplate.query(sql, RowMappers.USER);
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        var sql = "select id, name, role, email, password from USERS where email = ?";
        var userList = jdbcTemplate.query(sql, RowMappers.USER, email);
        return userList.stream().findAny();
    }
}
