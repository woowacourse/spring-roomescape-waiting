package roomescape.feature.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.SlotKey;

public interface ReservationRepository {

    List<Reservation> findAllReservations();

    Optional<Reservation> findLowestIdWaitingReservation(SlotKey slotKey);

    boolean existsActiveReservation(SlotKey slotKey);

    List<SlotKey> findDeadSlotKeys();

    List<Reservation> findReservationsByNameAndNotDeleted(ReserverName name);

    Optional<Reservation> findReservationByIdAndNotDeleted(Long id);

    List<Long> findTimeIdsByDateAndThemeIdAndNotDeleted(LocalDate localDate, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void changeStatus(Long id, long version, ReservationStatus from, ReservationStatus to);

    void changeOrderStatus(Long id, long version, OrderStatus from, OrderStatus to);

    int countByIdLessThanEqualAndSlot(Long id, SlotKey slotKey);

    boolean existsReservationByIdAndNotDeleted(Long id);

    boolean existsReservationAndStatus(Reservation reservation, ReservationStatus status);

    boolean existsActiveOrWaitingReservation(SlotKey slotKey);
}
