package roomescape.infra.user;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.application.exception.DuplicateResourceException;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.user.UserRole;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String FIND_BY_NAME_SQL = "select id, name, password, role from users where name = :name";
    private static final String EXISTS_BY_NAME_SQL = """
            select exists(
                select 1
                from users
                where name = :name
            )
            """;
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> User.of(
            rs.getLong(COLUMN_ID),
            rs.getString(COLUMN_NAME),
            rs.getString(COLUMN_PASSWORD),
            UserRole.valueOf(rs.getString(COLUMN_ROLE))
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
    public Optional<User> findByName(String name) {
        List<User> result = jdbcTemplate.query(
                FIND_BY_NAME_SQL,
                new MapSqlParameterSource().addValue(COLUMN_NAME, name),
                USER_ROW_MAPPER
        );
        return result.stream().findFirst();
    }

    @Override
    public User save(User user) {
        Number key;
        try {
            key = insertUser.executeAndReturnKey(new MapSqlParameterSource()
                    .addValue(COLUMN_NAME, user.getName())
                    .addValue(COLUMN_PASSWORD, user.getPassword())
                    .addValue(COLUMN_ROLE, user.getRole().name()));
        } catch (DuplicateKeyException exception) {
            throw new DuplicateResourceException(exception);
        }

        return User.of(extractId(key), user.getName(), user.getPassword(), user.getRole());
    }

    @Override
    public boolean existsByName(String name) {
        Boolean exists = jdbcTemplate.queryForObject(
                EXISTS_BY_NAME_SQL,
                new MapSqlParameterSource().addValue(COLUMN_NAME, name),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }
}
