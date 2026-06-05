package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;

public interface ReservationRepository {

    List<ReservationWithWaitingNumber> findReservationsByNotDeletedWithWaitingNumber();

    List<ReservationWithWaitingNumber> findReservationsByNameAndNotDeletedWithWaitingNumber(String name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    Optional<Long> lockReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    boolean existsActiveReservationByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    Optional<Long> lockActiveReservationBySchedule(
        LocalDate date, Long themeId, Long timeId);

    Optional<Long> lockFirstWaitingReservationBySchedule(
        LocalDate date, Long themeId, Long timeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void deleteReservationById(Long id);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);
}
