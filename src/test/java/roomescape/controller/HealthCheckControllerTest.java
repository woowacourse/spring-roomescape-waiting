package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;

@WebMvcTest(controllers = HealthCheckController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void GET_health_정상_응답을_반환한다() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
