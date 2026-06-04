package roomescape.user.infra.jdbc;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserRepository;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";

    private static final String INSERT_SQL = "insert into users(name) values (?)";
    private static final String FIND_ALL_SQL = "select id, name from users order by id";
    private static final String FIND_BY_ID_SQL = "select id, name from users where id = ?";
    private static final String FIND_BY_NAME_SQL = "select id, name from users where name = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return User.createWithId(id, user);
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, userRowMapper());
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> result = jdbcTemplate.query(FIND_BY_ID_SQL, userRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public Optional<User> findByName(String name) {
        List<User> result = jdbcTemplate.query(FIND_BY_NAME_SQL, userRowMapper(), name);
        return result.stream().findFirst();
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> User.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME)
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
