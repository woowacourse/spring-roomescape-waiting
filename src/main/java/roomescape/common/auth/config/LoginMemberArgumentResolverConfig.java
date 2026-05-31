package roomescape.common.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.auth.resolver.LoginMemberArgumentResolver;
import roomescape.member.repository.MemberRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class LoginMemberArgumentResolverConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberRepository));
    }

}
