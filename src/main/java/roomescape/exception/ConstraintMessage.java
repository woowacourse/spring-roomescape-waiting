package roomescape.exception;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

public enum ConstraintMessage {

    NOT_BLANK(NotBlank.class, (field, attrs) -> field + "은(는) 필수 입력값입니다."),
    NOT_NULL(NotNull.class, (field, attrs) -> field + "은(는) 필수 입력값입니다."),
    MIN(Min.class, (field, attrs) -> String.format("%s은(는) %s 이상이어야 합니다.", field, attrs.get("value"))),
    MAX(Max.class, (field, attrs) -> String.format("%s은(는) %s 이하여야 합니다.", field, attrs.get("value"))),
    SIZE(Size.class, (field, attrs) -> String.format("%s은(는) 최소 %s자 이상이어야 합니다.", field, attrs.get("min")));

    private static final String DEFAULT_MESSAGE = "유효하지 않은 값입니다.";

    private final Class<? extends Annotation> annotation;
    private final BiFunction<String, Map<String, Object>, String> formatter;

    ConstraintMessage(Class<? extends Annotation> annotation,
                      BiFunction<String, Map<String, Object>, String> formatter) {
        this.annotation = annotation;
        this.formatter = formatter;
    }

    public static String describe(String field, Class<? extends Annotation> annotation, Map<String, Object> attrs) {
        return Arrays.stream(values())
                .filter(c -> c.annotation == annotation)
                .map(c -> c.formatter.apply(field, attrs))
                .findFirst()
                .orElse(DEFAULT_MESSAGE);
    }
}
