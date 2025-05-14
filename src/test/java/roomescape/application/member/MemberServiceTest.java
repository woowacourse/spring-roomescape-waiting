package roomescape.application.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
        memberService = new MemberService(memberRepository);
    }

    @Test
    void 모든_회원_조회() {
        // given
        memberRepository.save(new Member("벨로", new Email("test1@email.com"), "1234", Role.NORMAL));
        memberRepository.save(new Member("서프", new Email("test2@email.com"), "1234", Role.NORMAL));

        // when
        List<MemberResult> results = memberService.findAll();

        // then
        assertThat(results)
                .isEqualTo(List.of(
                        new MemberResult(1L, "벨로"),
                        new MemberResult(2L, "서프")
                ));
    }
}
