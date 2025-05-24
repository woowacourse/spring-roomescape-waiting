package roomescape.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.ClockConfig;
import roomescape.integration.fixture.MemberDbFixture;
import roomescape.service.LoginService;
import roomescape.service.request.LoginRequest;

@Transactional
@SpringBootTest
@Import(ClockConfig.class)
class LoginServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 로그인에_성공한다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var request = new LoginRequest(member.getEmail().email(), "gustn111!!");

        // when
        var result = loginService.login(request);

        // then
        assertThat(result.getId()).isEqualTo(member.getId());
    }

    @Test
    void 존재하지_않는_이메일은_로그인할_수_없다() {
        // given
        var request = new LoginRequest("notfound@email.com", "gustn111!!");

        // when // then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void 비밀번호가_일치하지_않으면_로그인할_수_없다() {
        // given
        var member = memberDbFixture.한스_leehyeonsu4888_지메일_일반_멤버();
        var request = new LoginRequest(member.getEmail().email(), "wrongpassword");

        // when // then
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(IllegalStateException.class);
    }
}
