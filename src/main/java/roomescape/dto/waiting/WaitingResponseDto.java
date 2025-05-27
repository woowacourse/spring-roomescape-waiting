package roomescape.dto.waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponseDto(Long id, String name, LocalTime time, LocalDate date, String themeName) {

}
