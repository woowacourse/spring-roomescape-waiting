package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import roomescape.fixture.MemberDbFixture;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 유저가_가입한다() {
        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                "1234",
                "유저1"
        );

        MemberResponse response = memberService.signUp(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("유저1");
        assertThat(response.email()).isEqualTo("user1@email.com");
    }

    @Test
    void 유저를_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        Member member = memberService.getMember(유저1.getId());

        assertThat(member.getId()).isEqualTo(유저1.getId());
        assertThat(member.getName()).isEqualTo(유저1.getName());
        assertThat(member.getRole()).isEqualTo(유저1.getRole());
        assertThat(member.getEmail()).isEqualTo(유저1.getEmail());
    }

    @Test
    void 유저를_이메일과_비밀번호로_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        Member member = memberService.getMember(유저1.getEmail(), 유저1.getPassword());

        assertThat(member.getId()).isEqualTo(유저1.getId());
        assertThat(member.getName()).isEqualTo(유저1.getName());
        assertThat(member.getRole()).isEqualTo(유저1.getRole());
        assertThat(member.getEmail()).isEqualTo(유저1.getEmail());
    }

    @Test
    void 모든_유저를_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        MemberResponse 유저1_응답 = memberService.getMembers().get(0);

        assertThat(유저1_응답.id()).isEqualTo(유저1.getId());
        assertThat(유저1_응답.name()).isEqualTo(유저1.getName());
        assertThat(유저1_응답.email()).isEqualTo(유저1.getEmail());
    }
}
