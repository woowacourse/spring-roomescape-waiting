package roomescape.auth.sign.application.dto;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.validate.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record SignInResult(Cookie cookie) {

    public static SignInResult from(final Cookie cookie) {
        return new SignInResult(cookie);
    }

    public SignInResult{
        validate(cookie);
    }

    private void validate(final Cookie cookie) {
        Validator.of(SignInResult.class)
                .validateNotNull(Fields.cookie, cookie, "signInResult");

    }
}
