package roomescape.auth.domain;

import java.util.function.Supplier;

import roomescape.exception.custom.UnauthorizedException;

public class Payload<T> {
    private final T body;
    private final Supplier<Boolean> validator;

    public Payload(T body, Supplier<Boolean> validator) {
        this.body = body;
        this.validator = validator;
    }

    public T getValue() {
        if (!validator.get()) {
            throw new UnauthorizedException("토큰 페이로드 추출에 실패했습니다");
        }
        return body;
    }
}
