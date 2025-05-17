package roomescape.common.validate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URI;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final Class<?> clazz;

    public static Validator of(final Class<?> clazz) {
        return new Validator(clazz);
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

    public Validator validateEmailFormat(final String fieldName,
                                         final String target,
                                         final String fieldDescription) {
        validateNotBlank(fieldName, target, fieldDescription);

        if (EMAIL_PATTERN.matcher(target).matches()) {
            return this;
        }

        throw buildException(
                ValidationType.EMAIL_CHECK,
                fieldName,
                fieldDescription);
    }

    private InvalidInputException buildException(final ValidationType type,
                                                 final String fieldName,
                                                 final String fieldDescription) {
        return new InvalidInputException(
                type,
                clazz.getSimpleName(),
                fieldName,
                fieldDescription
        );
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum ValidationType {
        NULL_CHECK("while checking null"),
        BLANK_CHECK("while checking blank"),
        URI_CHECK("while checking URI"),
        EMAIL_CHECK("while checking email");

        private final String description;
    }
}
