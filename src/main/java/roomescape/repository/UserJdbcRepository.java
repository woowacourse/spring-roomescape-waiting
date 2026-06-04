package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.User;

@Repository
public class UserJdbcRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getString("username"),
            Password.ofHashed(rs.getString("password")),
            rs.getString("name"),
            Role.valueOf(rs.getString("role"))
    ).withId(rs.getLong("id"));

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, name, role FROM users WHERE username = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, username));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, password, name, role FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(User user) {
        String sql = "INSERT INTO users (username, password, name, role) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword().getValue());
            ps.setString(3, user.getName());
            ps.setString(4, user.getRoleName());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS(SELECT 1 from users where username = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, username));
    }
}
