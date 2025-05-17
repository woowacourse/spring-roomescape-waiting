package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.db.MemberDbFixture.RAW_PASSWORD;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.db.MemberDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberDbFixture memberDbFixture;

    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 유저가_가입한다() {
        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                "1234",
                "유저1"
        );

        MemberResponse response = memberService.signUp(request);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.id()).isNotNull();
        softly.assertThat(response.name()).isEqualTo("유저1");
        softly.assertThat(response.email()).isEqualTo("user1@email.com");
        softly.assertAll();
    }

    @Test
    void 이미_가입된_이메일이면_예외를_던진다() {
        memberDbFixture.유저1_생성();

        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                "1234",
                "유저1"
        );

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 유저를_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        Member member = memberService.getMember(유저1.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(member.getId()).isEqualTo(유저1.getId());
        softly.assertThat(member.getName()).isEqualTo(유저1.getName());
        softly.assertThat(member.getRole()).isEqualTo(유저1.getRole());
        softly.assertThat(member.getEmail()).isEqualTo(유저1.getEmail());
        softly.assertAll();
    }

    @Test
    void 유저를_이메일과_비밀번호로_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        Member member = memberService.getMember(유저1.getEmail(), RAW_PASSWORD);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(member.getId()).isEqualTo(유저1.getId());
        softly.assertThat(member.getName()).isEqualTo(유저1.getName());
        softly.assertThat(member.getRole()).isEqualTo(유저1.getRole());
        softly.assertThat(member.getEmail()).isEqualTo(유저1.getEmail());
        softly.assertAll();
    }

    @Test
    void 모든_유저를_조회한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        MemberResponse 유저1_응답 = memberService.getMembers().get(0);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(유저1_응답.id()).isEqualTo(유저1.getId());
        softly.assertThat(유저1_응답.name()).isEqualTo(유저1.getName());
        softly.assertThat(유저1_응답.email()).isEqualTo(유저1.getEmail());
        softly.assertAll();
    }

    @Test
    void 존재하지_않는_멤버를_조회하면_예외가_발생한다() {
        assertThatThrownBy(() -> memberService.getMember(999L))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }

    @Test
    void 잘못된_이메일로_로그인_시도시_예외가_발생한다() {
        memberDbFixture.유저1_생성();

        assertThatThrownBy(() -> memberService.getMember("wrong@email.com", RAW_PASSWORD))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 잘못된_비밀번호로_로그인_시도시_예외가_발생한다() {
        Member 유저1 = memberDbFixture.유저1_생성();

        assertThatThrownBy(() -> memberService.getMember(유저1.getEmail(), "wrongPassword"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 이메일_형식이_잘못된_경우_회원가입_실패() {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",
                "1234",
                "유저1"
        );

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("이메일 형식이 아닙니다.");
    }

    @Test
    void 비밀번호가_너무_긴_경우_회원가입_실패() {
        String tooLongPassword = "a".repeat(26); // 26자 비밀번호 생성

        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                tooLongPassword,
                "유저1"
        );

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("비밀번호는 공백이거나 25자 이상일 수 없습니다.");
    }

    @Test
    void 비밀번호가_공백인_경우_회원가입_실패() {
        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                "",
                "유저1"
        );

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("비밀번호는 공백이거나 25자 이상일 수 없습니다.");
    }

    @Test
    void 필수_필드가_누락된_경우_회원가입_실패() {
        SignUpRequest request = new SignUpRequest(
                "user1@email.com",
                "1234",
                null
        );

        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Member의 필드는 null 일 수 없습니다.");
    }
}
