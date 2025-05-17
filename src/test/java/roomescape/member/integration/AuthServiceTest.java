package roomescape.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.BadRequestException;
import roomescape.member.dto.request.LoginRequest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.AuthService;

@SpringBootTest
@Transactional
class AuthServiceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("로그인에 성공한다.")
    void login() {
        // given
        var loginRequest = new LoginRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when
        String token = authService.login(loginRequest);

        // then
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 실패한다.")
    void loginWithNonExistentEmail() {
        // given
        var loginRequest = new LoginRequest("wrong@email.com", DEFAULT_PASSWORD);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 실패한다.")
    void loginWithWrongPassword() {
        // given
        var loginRequest = new LoginRequest(DEFAULT_EMAIL, "wrong-password");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
} 
