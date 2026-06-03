package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingDetail(Long waitingId,
                            String username,
                            LocalDate date,
                            Long themeId,
                            String themeName,
                            String themeDescription,
                            String thumbnailImgUrl,
                            Long timeId,
                            LocalTime startAt,
                            int rank,
                            int totalRankCount) {
}
