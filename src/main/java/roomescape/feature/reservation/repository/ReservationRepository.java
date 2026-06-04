package roomescape.feature.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;

public interface ReservationRepository {

    List<Reservation> findAllReservations();

    Optional<Reservation> findLowestIdWaitingReservation(Slot slot);

    boolean existsActiveReservation(Slot slot);

    List<Slot> findDeadSlots();

    List<Reservation> findReservationsByNameAndNotDeleted(ReserverName name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    int changeStatus(Long id, ReservationStatus from, ReservationStatus to);

    int countByIdLessThanEqualAndSlot(Long id, Slot slot);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);

    boolean existsActiveOrWaitingReservation(Slot slot);
}
