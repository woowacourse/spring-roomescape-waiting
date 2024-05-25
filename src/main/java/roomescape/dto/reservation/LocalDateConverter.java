package roomescape.dto.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class LocalDateConverter {

    private LocalDateConverter() {

    }

    public static LocalDate toLocalDate(final String date) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("예약 날짜가 비어있습니다.");
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 예약 날짜입니다.");
        }
    }
}
