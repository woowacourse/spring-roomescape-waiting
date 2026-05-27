package roomescape.reservationWaiting.repository;

import java.time.LocalDate;
import roomescape.reservationWaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

//    List<ReservationWaiting> findAllByName(String name);
//
//    Optional<ReservationWaiting> findById(Long id);
//    List<ReservationWaiting> findAll();
//
    boolean existByDateAndTimeIdAndThemeIdAndName(LocalDate date, Long timeId, Long themeId, String name);
//
//    int deleteById(Long id);
}
