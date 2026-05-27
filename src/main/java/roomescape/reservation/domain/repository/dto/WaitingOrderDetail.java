package roomescape.reservation.domain.repository.dto;

import java.time.LocalDate;

public record WaitingOrderDetail(Long waitingId,
                                 String username,
                                 LocalDate date,
                                 Long themeId,
                                 Long timeId,
                                 Long order) {
}
