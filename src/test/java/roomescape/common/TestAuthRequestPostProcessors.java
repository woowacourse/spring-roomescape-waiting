package roomescape.common;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestAuthRequestPostProcessors {

    private static final String TEST_ROLE_HEADER = "X-Test-Role";

    private TestAuthRequestPostProcessors() {}

    public static RequestPostProcessor manager() {
        return request -> {
            request.addHeader(TEST_ROLE_HEADER, "MANAGER");
            return request;
        };
    }

    public static RequestPostProcessor member() {
        return request -> {
            request.addHeader(TEST_ROLE_HEADER, "MEMBER");
            return request;
        };
    }
}
