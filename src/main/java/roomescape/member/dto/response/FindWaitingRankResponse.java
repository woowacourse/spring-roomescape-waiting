package roomescape.member.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record FindWaitingRankResponse(Long waitingId,
                                      String theme,
                                      LocalDate date,
                                      LocalTime time,
                                      Long waitingNumber) {
}
