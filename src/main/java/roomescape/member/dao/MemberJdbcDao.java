package roomescape.member.dao;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberJdbcDao implements MemberDao {

    private final JdbcTemplate jdbcTemplate;

    public MemberJdbcDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Long> findIdByEmailAndPassword(String email, String password) {
        String findByEmailAndPasswordSql = """
                SELECT id
                FROM member
                WHERE email = ? AND password = ?
                """;

        try {
            Long id = jdbcTemplate.queryForObject(findByEmailAndPasswordSql,
                    (resultSet, rowNum) -> resultSet.getLong("id"), email, password);

            return Optional.ofNullable(id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findNameById(long id) {
        String findNameByIdSql = """
                SELECT name
                FROM member
                WHERE id =?
                """;

        try {
            String name = jdbcTemplate.queryForObject(findNameByIdSql,
                    (resultSet, rowNum) -> resultSet.getString("name"), id);

            return Optional.ofNullable(name);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Long> findAllId() {
        String findNameByIdSql = """
                SELECT id
                FROM member
                """;

        return jdbcTemplate.query(findNameByIdSql,
                (resultSet, rowNum) -> resultSet.getLong("id"));
    }

    @Override
    public Optional<String> findRoleById(long id) {
        String findRoleByIdSql = """
                SELECT role
                FROM member
                WHERE id =?
                """;

        try {
            String role = jdbcTemplate.queryForObject(findRoleByIdSql,
                    (resultSet, rowNum) -> resultSet.getString("role"), id);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
