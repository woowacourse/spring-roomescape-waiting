package roomescape.common.auth.resolver;

import static roomescape.common.auth.exception.AuthExceptionInformation.UN_AUTHORIZED;
import static roomescape.member.exception.MemberExceptionInformation.MEMBER_NOT_FOUND;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.common.auth.annotation.LoginMember;
import roomescape.common.auth.exception.AuthException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberException;
import roomescape.member.repository.MemberRepository;


@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    public LoginMemberArgumentResolver(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Long memberId = (Long) request.getAttribute("memberId");
        if (memberId == null) {
            throw new AuthException(UN_AUTHORIZED);
        }
        return getMember(memberId);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));
    }

}
