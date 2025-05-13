package roomescape.integration.fixture;

import static roomescape.integration.fixture.MemberEmailFixture.이메일_leehyeonsu4888지메일;
import static roomescape.integration.fixture.MemberNameFixture.한스;
import static roomescape.integration.fixture.MemberPasswordFixture.비밀번호_gustn111느낌표두개;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

@Component
public class MemberDbFixture {
    private final SimpleJdbcInsert simpleJdbcInsert;

    public MemberDbFixture(final JdbcTemplate jdbcTemplate) {
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    public Member 한스_leehyeonsu4888_지메일_일반_멤버() {
        return createMember(한스, 이메일_leehyeonsu4888지메일, 비밀번호_gustn111느낌표두개, MemberRole.MEMBER);
    }

    public Member leehyeonsu4888_지메일_gustn111느낌표두개() {
        return createMember(한스, 이메일_leehyeonsu4888지메일, 비밀번호_gustn111느낌표두개, MemberRole.MEMBER);
    }


    public Member createMember(
            final MemberName name,
            final MemberEmail email,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name.name())
                .addValue("email", email.email())
                .addValue("password", password.password())
                .addValue("role", role);
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Member(id, name, email, password, role);
    }
}
