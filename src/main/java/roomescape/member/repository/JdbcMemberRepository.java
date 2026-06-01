package roomescape.member.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Member> rowMapper = (resultSet, rowNum) -> Member.restore(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Member.restore(id, member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public Optional<Member> findById(Long id) {
        String query = "SELECT * FROM member WHERE id = ?";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String query = "SELECT * FROM member WHERE email = ?";
        return jdbcTemplate.query(query, rowMapper, email).stream().findFirst();
    }
}
