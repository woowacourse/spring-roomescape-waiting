package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import roomescape.member.Member;

class LoginMemberArgumentResolverTest {

    private final LoginMemberArgumentResolver resolver =
            new LoginMemberArgumentResolver(null, null);

    @Test
    void 어노테이션이_있고_Long타입이면_지원한다() throws NoSuchMethodException {
        MethodParameter parameter = methodParameter("withLongAnnotation", Long.class);
        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void 어노테이션이_있고_Member타입이면_지원한다() throws NoSuchMethodException {
        MethodParameter parameter = methodParameter("withMemberAnnotation", Member.class);
        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void 어노테이션이_없으면_지원하지_않는다() throws NoSuchMethodException {
        MethodParameter parameter = methodParameter("withoutAnnotation", Long.class);
        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void 어노테이션이_있어도_타입이_다르면_지원하지_않는다() throws NoSuchMethodException {
        MethodParameter parameter = methodParameter("wrongType", String.class);
        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    private MethodParameter methodParameter(String methodName, Class<?> parameterType)
            throws NoSuchMethodException {
        Method method = Fixtures.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private static class Fixtures {
        void withLongAnnotation(@LoginMember Long memberId) {
        }

        void withMemberAnnotation(@LoginMember Member member) {
        }

        void withoutAnnotation(Long memberId) {
        }

        void wrongType(@LoginMember String memberId) {
        }
    }
}
