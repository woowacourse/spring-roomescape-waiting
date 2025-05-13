package roomescape.admin.infrastructure;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import roomescape.admin.domain.Admin;
import roomescape.admin.domain.AdminRepository;

@Repository
public class JdbcAdminRepository implements AdminRepository {

    private final static RowMapper<Admin> ROW_MAPPER = (resultSet, rowNum) -> new Admin(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password")
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcAdminRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Admin findById(Long id) {
        String sql = "SELECT * FROM admin WHERE id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);

        return namedParameterJdbcTemplate.queryForObject(sql, param, ROW_MAPPER);
    }

    @Override
    public Admin findByEmail(String email) {
        String sql = "SELECT * FROM admin WHERE email = :email";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);
        
        return namedParameterJdbcTemplate.queryForObject(sql, param, ROW_MAPPER);
    }

    @Override
    public boolean isExistsByEmail(String email) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM admin
                    WHERE email = :email
                )
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);

        return namedParameterJdbcTemplate.queryForObject(sql, param, Boolean.class).booleanValue();
    }
}
