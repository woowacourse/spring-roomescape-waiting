package roomescape.common.validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.net.URI;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator {

    private final String className;

    public static Validator of(final Class<?> clazz) {
        return new Validator(clazz.getSimpleName());
    }

    public Validator validateNotNull(final String fieldName,
                                     final Object target,
                                     final String fieldDescription) {
        if (target == null) {
            throw buildException(
                    ValidationType.NULL_CHECK,
                    fieldName,
                    fieldDescription);
        }
        return this;
    }

    public Validator validateNotBlank(final String fieldName,
                                      final String target,
                                      final String fieldDescription) {
        if (target == null || target.strip().isBlank()) {
            throw buildException(
                    ValidationType.BLANK_CHECK,
                    fieldName,
                    fieldDescription);
        }
        return this;
    }

    public Validator validateUriFormat(final String fieldName,
                                       final String target,
                                       final String fieldDescription) {
        try {
            validateNotBlank(fieldName, target, fieldDescription);
            URI.create(target);
            return this;
        } catch (final IllegalArgumentException e) {
            throw buildException(
                    ValidationType.URI_CHECK,
                    fieldName,
                    fieldDescription);
        }
    }

    private InvalidArgumentException buildException(final ValidationType type,
                                                    final String fieldName,
                                                    final String fieldDescription) {
        return new InvalidArgumentException(
                type,
                className,
                fieldName,
                fieldDescription
        );
    }


}
