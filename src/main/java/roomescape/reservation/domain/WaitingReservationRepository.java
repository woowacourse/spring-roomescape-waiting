package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.util.List;
import java.util.Optional;

public interface WaitingReservationRepository {

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    Optional<WaitingReservation> findById(Long id);

    List<WaitingReservation> findAll();

    WaitingReservation save(WaitingReservation waitingReservation);

    int findMaxWaitingByParams(ReservationDate date, ReservationTime time, Theme theme);

    void deleteById(Long id);

    int decrementWaitingOrderAfter(ReservationDate date, ReservationTime time, Theme theme, int waitingOrder);
}
