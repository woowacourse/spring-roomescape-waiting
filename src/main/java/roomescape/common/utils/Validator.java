package roomescape.common.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import roomescape.common.exception.InvalidInputException;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator {

    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final Class<?> clazz;

    public static Validator of(final Class<?> clazz) {
        return new Validator(clazz);
    }

    public Validator notNullField(final String fieldName, final Object target) {
        if (target == null) {
            throw new InvalidInputException(clazz.getSimpleName() + "." + fieldName + " 은(는) null일 수 없습니다.");
        }
        return this;
    }

    public Validator notBlankField(final String fieldName, final String target) {
        if (target == null || target.isBlank()) {
            throw new InvalidInputException(clazz.getSimpleName() + "." + fieldName + " 은(는) 비어있을 수 없습니다.");
        }
        return this;
    }

    public Validator emailField(final String fieldName, final String target) {
        if (target == null || !target.matches(EMAIL_REGEX)) {
            throw new InvalidInputException(clazz.getSimpleName() + "." + fieldName + " 은(는) 유효한 이메일 형식이 아닙니다.");
        }
        return this;
    }
}
