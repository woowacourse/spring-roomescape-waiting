package roomescape.domain;

import java.time.LocalDate;

public record ReservationWithWaitingOrder(Long id,
                                          String reserverName,
                                          LocalDate date,
                                          ReservationTime time,
                                          Theme theme,
                                          ReservationStatus status,
                                          WaitingOrder waitingOrder) {
}
