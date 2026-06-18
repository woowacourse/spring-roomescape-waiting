package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.dto.ReservationWithWaitingNumber;
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
        select new roomescape.domain.reservation.dto.ReservationWithWaitingNumber(
            reservation,
            case
                when reservation.status = :waitingStatus then (
                    select count(waitingReservation) + 1
                    from Reservation waitingReservation
                    where waitingReservation.reservationSlot = slot
                      and waitingReservation.status = :waitingStatus
                      and (
                          waitingReservation.updatedAt < reservation.updatedAt
                          or (
                              waitingReservation.updatedAt = reservation.updatedAt
                              and waitingReservation.id < reservation.id
                          )
                      )
                )
                else null
            end
        )
        from Reservation reservation
        join reservation.user
        join reservation.reservationSlot slot
        join slot.date date
        join slot.time time
        join slot.theme
        order by date.date desc, time.startAt desc, reservation.id
        """)
    List<ReservationWithWaitingNumber> findReservationsForAdmin(ReservationStatus waitingStatus);

    @Query("""
        select new roomescape.domain.reservation.dto.ReservationWithWaitingNumber(
            reservation,
            (
                select count(waitingReservation) + 1
                from Reservation waitingReservation
                where waitingReservation.reservationSlot = slot
                  and waitingReservation.status = :status
                  and (
                      waitingReservation.updatedAt < reservation.updatedAt
                      or (
                          waitingReservation.updatedAt = reservation.updatedAt
                          and waitingReservation.id < reservation.id
                      )
                  )
            )
        )
        from Reservation reservation
        join reservation.user
        join reservation.reservationSlot slot
        join slot.date date
        join slot.time time
        join slot.theme
        where reservation.status = :status
        order by date.date desc, time.startAt desc, reservation.id
        """)
    List<ReservationWithWaitingNumber> findWaitingReservationsForAdmin(ReservationStatus status);

    @Query("""
        select new roomescape.domain.reservation.dto.ReservationWithWaitingNumber(
            reservation,
            case
                when reservation.status = :waitingStatus then (
                    select count(waitingReservation) + 1
                    from Reservation waitingReservation
                    where waitingReservation.reservationSlot = slot
                      and waitingReservation.status = :waitingStatus
                      and (
                          waitingReservation.updatedAt < reservation.updatedAt
                          or (
                              waitingReservation.updatedAt = reservation.updatedAt
                              and waitingReservation.id < reservation.id
                          )
                      )
                )
                else null
            end
        )
        from Reservation reservation
        join reservation.user user
        join reservation.reservationSlot slot
        join slot.date date
        join slot.time time
        join slot.theme
        where user.name = :username
        order by date.date desc, time.startAt desc, reservation.id
        """)
    List<ReservationWithWaitingNumber> findUserReservations(String username, ReservationStatus waitingStatus);

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
