package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberPassword;
import roomescape.exception.member.EmailDuplicatedException;
import roomescape.exception.member.UnauthorizedEmailException;
import roomescape.exception.member.UnauthorizedPasswordException;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberLoginRequest;
import roomescape.service.helper.Encryptor;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberServiceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private MemberRepository memberRepository;

    private final Member member1 = new Member("t1@t1.com", "123", "러너덕", "MEMBER");
    private final Member member3 = new Member("t3@t3.com", "125", "재즈덕", "MEMBER");

    @DisplayName("이메일이 중복인 회원을 생성하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_create_duplicated_member_email() {
        memberRepository.save(member1);

        MemberCreateRequest requestDto = new MemberCreateRequest("t1@t1.com", "11", "워니");

        assertThatThrownBy(() -> memberService.signup(requestDto))
                .isInstanceOf(EmailDuplicatedException.class)
                .hasMessage("이미 가입되어 있는 이메일 주소입니다.");
    }

    @DisplayName("회원을 정상적으로 생성한다.")
    @Test
    void success_signup_member() {
        MemberCreateRequest requestDto = new MemberCreateRequest("t1@t1.com", "11", "워니");

        assertThatNoException()
                .isThrownBy(() -> memberService.signup(requestDto));
    }

    @DisplayName("로그인 시 저장되어있지 않은 이메일이면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_login_not_saved__member_email() {
        MemberLoginRequest requestDto = new MemberLoginRequest("t4@t4.com", "1212");

        assertThatThrownBy(() -> memberService.login(requestDto))
                .isInstanceOf(UnauthorizedEmailException.class)
                .hasMessage("이메일이 존재하지 않습니다.");
    }

    @DisplayName("로그인 시 비밀번호가 일치하지 않으면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_is_mismatched_password() {
        memberRepository.save(member3);

        MemberLoginRequest requestDto = new MemberLoginRequest("t3@t3.com", "1212");

        assertThatThrownBy(() -> memberService.login(requestDto))
                .isInstanceOf(UnauthorizedPasswordException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("로그인이 정상적으로 완료되고 토큰을 발급한다.")
    @Test
    void success_login() {
        MemberPassword encryptPassword = encryptor.encryptPassword("125");
        Member savedMember = memberRepository.save(new Member("t3@t3.com", encryptPassword, "영이"));
        String expectedToken = jwtManager.generateToken(savedMember);
        MemberLoginRequest requestDto = new MemberLoginRequest("t3@t3.com", "125");

        String actualToken = memberService.login(requestDto);

        assertThat(actualToken).isEqualTo(expectedToken);
    }
}
