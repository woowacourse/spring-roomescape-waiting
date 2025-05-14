package roomescape.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.application.dto.MemberResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    void 멤버를_모두_조회한다() {
        // given

        // when & then
        assertThat(memberService.findAll()).hasSize(4);
    }

    @Test
    void id로_멤버를_조회한다() {
        // given
        final Long id = 1L;

        // when & then
        assertThat(memberService.findById(id)).isEqualTo(
                new MemberResponse(1L, "엠제이")
        );

    }
}
