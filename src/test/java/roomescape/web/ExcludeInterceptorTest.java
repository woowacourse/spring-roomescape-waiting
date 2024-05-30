package roomescape.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.SpyBean;
import roomescape.web.config.AdminHandlerInterceptor;

abstract class ExcludeInterceptorTest extends ControllerTest {

    @SpyBean
    private AdminHandlerInterceptor adminHandlerInterceptor;

    @BeforeEach
    void setUp() {
        doReturn(true).when(adminHandlerInterceptor).preHandle(any(), any(), any());
    }
}
