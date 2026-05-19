package roomescape.common.auth.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import roomescape.common.handler.TokenReturnValueHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class ReturnValueHandlerConfig implements InitializingBean {

    private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    public ReturnValueHandlerConfig(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();
        handlers.add(new TokenReturnValueHandler());
        handlers.addAll(Objects.requireNonNull(requestMappingHandlerAdapter.getReturnValueHandlers()));
        requestMappingHandlerAdapter.setReturnValueHandlers(handlers);
    }

}
