package roomescape.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.domain.user.Role;
import roomescape.exception.ForbiddenException;
import roomescape.service.dto.output.TokenLoginOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class CheckAdminInterceptorTest {

    CheckAdminInterceptor sut;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        sut = new CheckAdminInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("토큰의 정보가 관리자이면 참을 반환한다.")
    void return_true_when_token_info_is_admin() {
        final var output = new TokenLoginOutput(1, "운영자", "admin@email.com", "password1234", Role.ADMIN.getValue());
        request.setAttribute("member", output);

        final var result = sut.preHandle(request, response, null);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰의 정보가 관리자가 아니면 예외를 발생한다.")
    void throw_exception_when_token_info_is_not_admin() {
        final var output = new TokenLoginOutput(1, "조이썬", "joyson5582@email.com", "password1234", Role.USER.getValue());
        request.setAttribute("member", output);

        assertThatThrownBy(() -> sut.preHandle(request, response, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("토큰이 없으면 예외를 발생한다.")
    void return_false_when_not_exist_token() {
        assertThatThrownBy(() -> sut.preHandle(request, response, null))
                .isInstanceOf(ForbiddenException.class);
    }
}
