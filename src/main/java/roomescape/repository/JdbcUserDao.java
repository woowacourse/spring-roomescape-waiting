package roomescape.repository;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.model.Role;
import roomescape.model.User;

@Repository
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertActor;

    private static final RowMapper<User> userRowMapper = (resultSet, rowNum) ->
            new User(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    Role.valueOf(resultSet.getString("role")),
                    resultSet.getString("email"),
                    resultSet.getString("password")
            );

    public JdbcUserDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<User> findUserByEmailAndPassword(String email, String password) {
        String sql = "SELECT id, name, role, email, password FROM users WHERE email = ? AND password = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, email, password));
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findUserNameByUserId(Long userId) {
        String sql = "SELECT name FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, userId));
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        String sql = "SELECT id, name, role, email, password FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, userId));
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT id, name, role, email, password FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

}
