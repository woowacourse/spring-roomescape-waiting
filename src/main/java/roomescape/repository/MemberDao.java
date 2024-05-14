package roomescape.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.dto.LoginRequest;
import roomescape.domain.dto.SignupRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberDao {
    private static final RowMapper<Member> memberRowMapper =
            (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("email"),
                    resultSet.getString("name"),
                    resultSet.getString("role")
            ) {
            };

    private static final RowMapper<Password> passwordRowMapper =
            (resultSet, rowNum) -> new Password(
                    resultSet.getString("password"),
                    resultSet.getString("salt")
            ) {
            };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingColumns("email", "password", "salt", "name", "role")
                .usingGeneratedKeyColumns("id");
    }

    public List<Member> findAll() {
        String sql = "select id, email, name, role from member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    public Optional<Member> findByEmail(final String email) {
        String sql = "select id, email, name, role from member where email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, memberRowMapper, email));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Member> findById(final Long memberId) {
        String sql = "select id, email, name, role from member where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, memberRowMapper, memberId));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Long create(final SignupRequest signupRequest, final Password password) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("email", signupRequest.email())
                .addValue("password", password.getHashValue())
                .addValue("salt", password.getSalt())
                .addValue("name", signupRequest.name())
                .addValue("role", "USER");
        return jdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    public boolean isExist(final SignupRequest signupRequest) {
        String sql = "select count(email) from member where email = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, signupRequest.email()) != 0;
    }

    public boolean isLoginFail(final LoginRequest loginRequest, Password password) {
        String sql = "select count(*) from member where email = ? and password = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, loginRequest.email(), password.getHashValue()) == 0;
    }

    public Optional<Password> findPasswordByEmail(final String email) {
        String sql = "select password, salt from member where email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, passwordRowMapper, email));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
}
