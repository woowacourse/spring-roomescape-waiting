package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminCheckInterceptor;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.MemberArgumentResolver;
import roomescape.member.repository.MemberRepository;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public WebMvcConfiguration(final MemberRepository memberRepository, final JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberArgumentResolver(memberRepository, jwtTokenProvider));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminCheckInterceptor(memberRepository, jwtTokenProvider))
                .addPathPatterns("/admin/**");
    }
}
