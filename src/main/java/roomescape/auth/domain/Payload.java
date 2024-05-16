package roomescape.auth.domain;

import java.util.function.Supplier;
import roomescape.exception.AuthenticationException;
import roomescape.exception.ErrorType;

public class Payload<T> {
    private final T body;
    private final Supplier<Boolean> validator;

    public Payload(T body, Supplier<Boolean> validator) {
        this.body = body;
        this.validator = validator;
    }

    public T getValue() {
        if (!validator.get()) {
            throw new AuthenticationException(ErrorType.TOKEN_PAYLOAD_EXTRACTION_FAILURE);
        }
        return body;
    }
}
