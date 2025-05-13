package roomescape.global;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.repository.MemberRepository;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberRepository memberRepository;

    public WebConfig(final MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberArgumentResolver());
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminOnlyInterceptor(memberRepository))
                .addPathPatterns("/admin/**");
    }
}
