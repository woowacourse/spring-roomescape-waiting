package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.vo.ReservationSchedule;

public interface ReservationRepository {

    List<ReservationWithWaitingNumber> findReservationsByNotDeletedWithWaitingNumber();

    List<ReservationWithWaitingNumber> findReservationsByNameAndNotDeletedWithWaitingNumber(String name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    Optional<Long> lockReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    boolean existsActiveReservationBySchedule(ReservationSchedule schedule);

    Optional<Long> lockActiveReservationBySchedule(ReservationSchedule schedule);

    Optional<Long> lockFirstWaitingReservationBySchedule(ReservationSchedule schedule);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void deleteReservationById(Long id);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);
}
