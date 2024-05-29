package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.TokenLoginOutput;

public record TokenLoginResponse(String name) {
    public static TokenLoginResponse from(final TokenLoginOutput output) {
        return new TokenLoginResponse(output.name());
    }
}
