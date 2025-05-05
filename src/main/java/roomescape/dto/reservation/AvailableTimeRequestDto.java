package roomescape.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record AvailableTimeRequestDto(@FutureOrPresent(message = "날짜는 현재보다 미래여야합니다.") LocalDate date, Long themeId) {

}
