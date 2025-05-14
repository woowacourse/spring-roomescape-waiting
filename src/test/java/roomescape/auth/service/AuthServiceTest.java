package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.stub.StubTokenProvider;
import roomescape.auth.web.controller.request.LoginRequest;
import roomescape.auth.web.controller.response.MemberNameResponse;
import roomescape.config.AuthServiceTestConfig;
import roomescape.fixture.MemberDbFixture;
import roomescape.member.domain.Member;

@Import(AuthServiceTestConfig.class)
@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 로그인한다() {
        Member user = memberDbFixture.유저1_생성();
        LoginRequest request = new LoginRequest(user.getEmail(), user.getPassword());

        String accessToken = authService.login(request);

        assertThat(accessToken).isEqualTo(StubTokenProvider.USER_STUB_TOKEN);
    }

    @Test
    void 로그인한_사용자인지_확인한다() {
        Member user = memberDbFixture.유저1_생성();

        MemberNameResponse response = authService.checkLogin(user.getId());

        assertThat(response.name()).isEqualTo(user.getName());
    }

    @Test
    void 관리자인지_확인한다__관리자면_true() {
        Member admin = memberDbFixture.관리자_생성();
        String token = authService.login(new LoginRequest(admin.getEmail(), admin.getPassword()));

        boolean isAdmin = authService.isAdmin(token);

        assertThat(isAdmin).isTrue();
    }

    @Test
    void 관리자인지_확인한다__관리자가_아니면_false() {
        Member user = memberDbFixture.유저1_생성();
        String token = authService.login(new LoginRequest(user.getEmail(), user.getPassword()));

        boolean isAdmin = authService.isAdmin(token);

        assertThat(isAdmin).isFalse();
    }

    @Test
    void 토큰에서_회원_ID를_추출한다() {
        Member user = memberDbFixture.유저1_생성();
        String token = authService.login(new LoginRequest(user.getEmail(), user.getPassword()));

        Long memberId = authService.getMemberId(token);

        assertThat(memberId).isEqualTo(StubTokenProvider.STUB_MEMBER_ID);
    }
}
