package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingOrder;

public record ReservationWithWaitingOrder(Long id,
                                          String reserverName,
                                          LocalDate date,
                                          ReservationTime time,
                                          Theme theme,
                                          WaitingOrder waitingOrder) {
}
