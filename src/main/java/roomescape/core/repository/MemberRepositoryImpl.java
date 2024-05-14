package roomescape.core.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.core.domain.Member;
import roomescape.core.domain.Role;

@Repository
public class MemberRepositoryImpl implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Member member) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());
        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public Member findByEmailAndPassword(final String email, final String password) {
        final String query = "SELECT id, name, email, password, role FROM member WHERE email = ? AND password = ?";
        return jdbcTemplate.queryForObject(query, getMemberRowMapper(), email, password);
    }

    @Override
    public Member findByEmail(final String email) {
        final String query = "SELECT id, name, email, password, role FROM member WHERE email = ?";
        return jdbcTemplate.queryForObject(query, getMemberRowMapper(), email);
    }

    @Override
    public List<Member> findAll() {
        final String query = "SELECT id, name, email, password, role FROM member";
        return jdbcTemplate.query(query, getMemberRowMapper());
    }

    @Override
    public Member findById(final Long memberId) {
        final String query = "SELECT id, name, email, password, role FROM member WHERE id = ?";
        return jdbcTemplate.queryForObject(query, getMemberRowMapper(), memberId);
    }

    private RowMapper<Member> getMemberRowMapper() {
        return (resultSet, rowNum) -> {
            final Long memberId = resultSet.getLong("id");
            final String name = resultSet.getString("name");
            final String email = resultSet.getString("email");
            final String password = resultSet.getString("password");
            final Role role = Role.valueOf(resultSet.getString("role"));

            return new Member(memberId, name, email, password, role);
        };
    }
}
