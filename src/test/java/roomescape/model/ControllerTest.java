package roomescape.model;

import org.springframework.boot.test.mock.mockito.MockBean;
import roomescape.auth.provider.model.TokenProvider;
import roomescape.auth.resolver.TokenResolver;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.MemberRoleInterceptor;

public abstract class ControllerTest {

    @MockBean
    private TokenResolver tokenResolver;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private MemberRoleInterceptor memberRoleInterceptor;
}
