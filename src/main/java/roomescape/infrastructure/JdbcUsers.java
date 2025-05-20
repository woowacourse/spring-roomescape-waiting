package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcUsers implements Users {

    private static final RowMapper<User> ROW_MAPPER = (resultSet, rowNum) -> User.restore(
            resultSet.getString("id"),
            resultSet.getString("role"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insert;

    public JdbcUsers(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users");
    }

    @Override
    public void save(final User user) {
        insert.execute(Map.of(
                "id", user.getId().value(),
                "role", user.getUserRole().name(),
                "email", user.getEmail().value(),
                "name", user.getName().value(),
                "password", user.getPassword().value()
        ));
    }

    @Override
    public List<User> findAll() {
        final String sql = """
                SELECT * FROM users
                """;

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Optional<User> findById(final Id userId) {
        final String sql = """
                SELECT * FROM users
                WHERE id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, userId.value()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        final String sql = """
                SELECT * FROM users
                WHERE email = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByEmail(final String email) {
        final String sql = """
                SELECT COUNT(*) FROM users
                WHERE email = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
