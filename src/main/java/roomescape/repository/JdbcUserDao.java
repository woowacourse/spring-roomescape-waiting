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
import roomescape.model.Member;

@Repository
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertActor;

    private static final RowMapper<Member> userRowMapper = (resultSet, rowNum) ->
            new Member(
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
    public Optional<Member> findUserByEmailAndPassword(String email, String password) {
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
    public Optional<Member> findUserById(Long userId) {
        String sql = "SELECT id, name, role, email, password FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, userId));
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAllUsers() {
        String sql = "SELECT id, name, role, email, password FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

}
