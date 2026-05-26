package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;

@JdbcTest
@ActiveProfiles("test")
@Import(MemberDao.class)
public class MemberDaoTest {

    private static final String INSERT_SINGLE_MEMBER_SQL = """
              INSERT INTO member (id, email, password, name)
              VALUES (1, 'brown@email.com', 'password', '브라운');
              """;

    @Autowired
    private MemberDao memberDao;

    @Test
    @Sql(statements = INSERT_SINGLE_MEMBER_SQL)
    void 이메일에_해당하는_회원을_조회한다() {
        Member member = memberDao.findByEmail("brown@email.com");

        assertThat(member).isNotNull();
        assertThat(member)
                .extracting(
                        Member::getId,
                        Member::getEmail,
                        Member::getName
                )
                .containsExactly(
                        1L,
                        "brown@email.com",
                        "브라운"
                );
        assertThat(member.matchesPassword("password")).isTrue();
    }
}
