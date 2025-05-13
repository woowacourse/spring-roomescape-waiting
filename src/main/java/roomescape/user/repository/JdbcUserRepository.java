package roomescape.user.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.RowMapperManager;
import roomescape.user.domain.User;
import roomescape.user.exception.NotFoundUserException;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        Long id = insertWithKeyHolder(user);
        return findByIdOrThrow(id);
    }

    @Override
    public List<User> findAll() {
        String sql = "select id AS user_id, role AS user_role, name AS user_name, email AS user_email, password AS user_password from users";
        return jdbcTemplate.query(sql, RowMapperManager.userRowMapper);
    }

    @Override
    public User findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundUserException("해당 유저 id가 존재하지 않습니다."));
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "select id AS user_id, role AS user_role, name AS user_name, email AS user_email, password AS user_password from users where id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RowMapperManager.userRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existUserByEmailAndPassword(String email, String password) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ? AND password = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, email, password);
    }

    @Override
    public Optional<User> findUseByEmail(String email) {
        String sql = "select id AS user_id, role AS user_role, name AS user_name, email AS user_email, password AS user_password from users where email = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RowMapperManager.userRowMapper, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserByEmailAndPassword(String email, String password) {
        String sql =
                "select id AS user_id, role AS user_role, name AS user_name, email AS user_email, password AS user_password "
                        + "FROM users "
                        + "WHERE email = ? AND password = ?";

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, RowMapperManager.userRowMapper, email, password));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Long insertWithKeyHolder(User user) {
        String sql = "INSERT INTO users (role, name, email, password) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id"});
            ps.setString(1, user.getRole().name());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
