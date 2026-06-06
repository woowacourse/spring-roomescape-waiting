package roomescape.infra.user;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String FIND_ALL_SQL = "select id, name from users order by id";
    private static final String FIND_BY_NAME_SQL = "select id, name from users where name = :name";
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> User.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertUser;

    public JdbcUserRepository(NamedParameterJdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertUser = new SimpleJdbcInsert(dataSource)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public User save(User user) {
        Number key = insertUser.executeAndReturnKey(new MapSqlParameterSource()
                .addValue(COLUMN_NAME, user.getName()));
        return User.of(extractId(key), user.getName());
    }

    public List<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new MapSqlParameterSource(), USER_ROW_MAPPER);
    }

    @Override
    public Optional<User> findByName(String name) {
        List<User> result = jdbcTemplate.query(
                FIND_BY_NAME_SQL,
                new MapSqlParameterSource().addValue(COLUMN_NAME, name),
                USER_ROW_MAPPER
        );
        return result.stream().findFirst();
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }
}
