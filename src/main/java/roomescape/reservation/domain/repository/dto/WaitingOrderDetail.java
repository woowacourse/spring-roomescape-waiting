package roomescape.reservation.domain.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingOrderDetail(Long waitingId,
                                 String username,
                                 LocalDate date,
                                 Long themeId,
                                 String themeName,
                                 String themeDescription,
                                 String thumbnailImgUrl,
                                 Long timeId,
                                 LocalTime startAt,
                                 Long order) {
}
