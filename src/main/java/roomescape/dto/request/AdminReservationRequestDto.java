package roomescape.dto.request;

import java.time.LocalDate;

public record AdminReservationRequestDto(LocalDate date, Long themeId, Long timeId, Long memberId) {

}
