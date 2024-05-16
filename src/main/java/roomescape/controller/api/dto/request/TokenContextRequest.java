package roomescape.controller.api.dto.request;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import roomescape.exception.UnauthorizedException;
import roomescape.service.dto.output.TokenLoginOutput;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@Component
@Scope(value = SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TokenContextRequest {
    private TokenLoginOutput tokenLoginOutput;

    public TokenLoginOutput getTokenLoginOutput() {
        if(tokenLoginOutput == null) {
            throw new UnauthorizedException();
        }
        return tokenLoginOutput;
    }

    public void setTokenLoginOutput(final TokenLoginOutput tokenLoginOutput) {
        this.tokenLoginOutput = tokenLoginOutput;
    }
}
