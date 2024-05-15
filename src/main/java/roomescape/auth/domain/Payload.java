package roomescape.auth.domain;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

import java.util.function.Supplier;

public class Payload<T> {
    private final T body;
    private final Supplier<Boolean> validator;

    public Payload(T body, Supplier<Boolean> validator) {
        this.body = body;
        this.validator = validator;
    }

    public T getValue() {
        if (!validator.get()) {
            throw new BusinessException(ErrorType.SECURITY_EXCEPTION);
        }
        return body;
    }
}
