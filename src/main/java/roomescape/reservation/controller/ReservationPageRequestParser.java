package roomescape.reservation.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

@Component
public class ReservationPageRequestParser {

    public Long parseLongValue(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "숫자 형식의 값이 필요합니다."
            );
        }
    }

    public LocalDate parseDate(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidInputException(
                    ErrorCode.INVALID_INPUT,
                    "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다."
            );
        }
    }
}
