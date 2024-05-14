package roomescape.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberCommandRepository;

@Repository
public class JdbcMemberCommandRepository implements MemberCommandRepository {
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcMemberCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingColumns("name", "email", "password", "role")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member create(Member member) {
        MapSqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());
        long id = jdbcInsert.executeAndReturnKey(parameter).longValue();
        return member.withId(id);
    }
}
