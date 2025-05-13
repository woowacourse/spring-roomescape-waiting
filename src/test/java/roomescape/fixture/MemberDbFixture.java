package roomescape.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Role;

@Component
public class MemberDbFixture {

    private final SimpleJdbcInsert jdbcInsert;

    public MemberDbFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    public Member 한스_사용자() {
        String name = "한스";
        Role role = Role.USER;
        String email = "test@test.com";
        String password = "pass1";

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("role", role.name())
                .addValue("email", email)
                .addValue("password", password)
        ).longValue();

        return Member.create(id, name, role, email, password);
    }
}
