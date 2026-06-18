package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.theme.Theme;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndStatusNot(Long id, ReservationStatus status);

    @Query("""
        select count(reservation)
        from Reservation reservation
        where reservation.reservationSlot.id = :reservationSlotId
          and reservation.status <> :excludedStatus
        """)
    Long countActiveReservationsInSlot(Long reservationSlotId, ReservationStatus excludedStatus);

    boolean existsByUserIdAndReservationSlotIdAndStatusNot(
        Long userId,
        Long reservationSlotId,
        ReservationStatus status
    );

    @Query("""
        select reservation
        from Reservation reservation
        where reservation.reservationSlot.id = :reservationSlotId
          and reservation.status = :status
        order by reservation.updatedAt, reservation.id
        """)
    List<Reservation> findWaitingReservationsForPromotion(
        Long reservationSlotId,
        ReservationStatus status
    );

    @Query("""
        select reservation
        from Reservation reservation
        join fetch reservation.user
        join fetch reservation.reservationSlot slot
        join fetch slot.date date
        join fetch slot.time time
        join fetch slot.theme
        order by date.date desc, time.startAt desc, reservation.id
        """)
    List<Reservation> findReservationsForAdmin();

    @Query("""
        select reservation
        from Reservation reservation
        join fetch reservation.user user
        join fetch reservation.reservationSlot slot
        join fetch slot.date date
        join fetch slot.time time
        join fetch slot.theme
        where user.name = :username
        order by date.date desc, time.startAt desc, reservation.id
        """)
    List<Reservation> findUserReservations(String username);

    @Query("""
        select reservation
        from Reservation reservation
        join fetch reservation.reservationSlot slot
        where slot.id in :reservationSlotIds
          and reservation.status = :status
        order by slot.id, reservation.updatedAt, reservation.id
        """)
    List<Reservation> findWaitingReservationsInSlots(
        List<Long> reservationSlotIds,
        ReservationStatus status
    );

    @Query("""
        select reservation
        from Reservation reservation
        join fetch reservation.reservationSlot slot
        join fetch slot.time
        where slot.theme.id = :themeId
          and slot.date.id = :dateId
          and reservation.status <> :excludedStatus
        """)
    List<Reservation> findReservationsForSlotAvailability(
        Long themeId,
        Long dateId,
        ReservationStatus excludedStatus
    );

    @Query("""
        select slot.theme
        from Reservation reservation
        join reservation.reservationSlot slot
        join slot.date date
        where date.date between :startDay and :today
          and reservation.status <> :excludedStatus
        """)
    List<Theme> findThemesForRanking(
        LocalDate startDay,
        LocalDate today,
        ReservationStatus excludedStatus
    );
}
