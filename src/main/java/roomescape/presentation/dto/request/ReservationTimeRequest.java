package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record ReservationTimeRequest(
        @NotNull @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "시간은 HH:mm 형식이어야 합니다.")
        String startAt
) {
    private static final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public LocalTime startAtToLocalTime() {
        return LocalTime.parse(startAt, datetimeFormatter);
    }
}
