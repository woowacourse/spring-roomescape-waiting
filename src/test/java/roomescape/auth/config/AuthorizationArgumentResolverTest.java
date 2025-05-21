package roomescape.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import roomescape.auth.LoginInfo;
import roomescape.exception.auth.AuthorizationException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationArgumentResolverTest {

    @Mock
    private MethodParameter parameter;

    @Mock
    private NativeWebRequest webRequest;

    @InjectMocks
    private AuthorizationArgumentResolver sut;

    @Test
    void LoginInfo_파라미터를_지원하면_true를_반환한다() {
        // given
        given(parameter.getParameterType()).willReturn((Class) LoginInfo.class);

        // when
        boolean result = sut.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void LoginInfo가_아닌_파라미터는_false를_반환한다() {
        // given
        given(parameter.getParameterType()).willReturn((Class) String.class);

        // when
        boolean result = sut.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void authorization_속성이_있으면_해당_객체를_반환한다() {
        // given
        LoginInfo loginInfo = mock(LoginInfo.class);
        given(webRequest.getAttribute("authorization", RequestAttributes.SCOPE_REQUEST))
                .willReturn(loginInfo);

        // when
        Object result = sut.resolveArgument(parameter, null, webRequest, null);

        // then
        assertThat(result).isEqualTo(loginInfo);
    }

    @Test
    void authorization_속성이_없으면_예외를_던진다() {
        // given
        given(webRequest.getAttribute("authorization", RequestAttributes.SCOPE_REQUEST))
                .willReturn(null);

        // when, then
        assertThatThrownBy(() -> sut.resolveArgument(parameter, null, webRequest, null))
                .isInstanceOf(AuthorizationException.class);
    }
}
