package roomescape.reservation.domain.repository.dto;

import java.time.LocalDate;

public record WaitingDetail(Long waitingId,
                            String username,
                            LocalDate date,
                            Long themeId,
                            Long timeId,
                            Long order) {
}
