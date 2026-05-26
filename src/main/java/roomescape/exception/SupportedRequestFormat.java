package roomescape.exception;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;

public enum SupportedRequestFormat {

    DATE("yyyy-MM-dd", LocalDate.class),
    TIME("HH:mm", LocalTime.class),
    NUMBER("숫자", Long.class, Integer.class, long.class, int.class);

    private static final String DEFAULT_DESCRIPTION = "올바른";

    private final String description;
    private final Set<Class<?>> types;

    SupportedRequestFormat(String description, Class<?>... types) {
        this.description = description;
        this.types = Set.of(types);
    }

    public static String describe(Class<?> targetType) {
        return Arrays.stream(values())
                .filter(format -> format.types.contains(targetType))
                .map(format -> format.description)
                .findFirst()
                .orElse(DEFAULT_DESCRIPTION);
    }
}