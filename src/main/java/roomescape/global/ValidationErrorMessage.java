package roomescape.global;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

public enum ValidationErrorMessage implements ErrorMessage {

    INVALID_TIME_FORMAT(LocalTime.class, "시간 형식은 HH:mm 이어야 합니다."),
    INVALID_DATE_FORMAT(LocalDate.class, "날짜 형식은 yyyy-MM-dd 이어야 합니다."),
    ;

    private final Class<?> targetType;
    private final String message;

    ValidationErrorMessage(Class<?> targetType, String message) {
        this.targetType = targetType;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static Optional<String> findMessageByType(Class<?> type) {
        return Arrays.stream(values())
                .filter(e -> e.targetType == type)
                .map(ValidationErrorMessage::getMessage)
                .findFirst();
    }
}
