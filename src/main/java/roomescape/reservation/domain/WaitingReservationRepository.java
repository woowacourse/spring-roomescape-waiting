package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.util.Optional;

public interface WaitingReservationRepository {

    Optional<WaitingReservation> findById(Long id);

    WaitingReservation save(WaitingReservation waitingReservation);

    int findMaxWaitingByParams(ReservationDate date, ReservationTime time, Theme theme);

    boolean existsByParams(Long id);

    void deleteById(Long id);

    int decrementWaitingOrderAfter(ReservationDate date, ReservationTime time, Theme theme, int waitingOrder);
}
