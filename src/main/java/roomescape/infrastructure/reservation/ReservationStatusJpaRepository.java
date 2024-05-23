package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public interface ReservationStatusJpaRepository extends ReservationStatusRepository,
        ListCrudRepository<ReservationStatus, Long> {

    List<BookStatus> NON_CANCELLED_STATUSES = List.of(
            BookStatus.WAITING, BookStatus.BOOKED
    );


    @Override
    default Optional<ReservationStatus> findFirstWaiting(Reservation reservation) {
        return findFirstWaiting(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                BookStatus.WAITING
        );
    }

    @Query("""
            select rs from ReservationStatus rs
            left join rs.reservation r on rs.reservation.id = r.id
            where r.theme = :theme
            and r.date = :date
            and r.time = :time
            and rs.status = :status
            order by rs.reservation.createdAt asc
            limit 1
             """)
    Optional<ReservationStatus> findFirstWaiting(
            Theme theme, LocalDate date, ReservationTime time, BookStatus status
    );

    @Override
    default long getWaitingCount(Reservation reservation) {
        return getWaitingCount(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                NON_CANCELLED_STATUSES,
                reservation.getCreatedAt()
        );
    }

    @Query("""
            select count(rs) from ReservationStatus rs
            left join rs.reservation r on rs.reservation.id = r.id
            where r.theme = :theme
            and r.date = :date
            and r.time = :time
            and rs.status in :statuses
            and rs.reservation.createdAt < :createdAt
            """)
    long getWaitingCount(
            Theme theme, LocalDate date, ReservationTime time, List<BookStatus> statuses, LocalDateTime createdAt
    );

    @Override
    default List<ReservationStatus> findActiveReservationStatusesByMemberId(long memberId) {
        return findAllByStatusInAndReservationMemberId(NON_CANCELLED_STATUSES, memberId);
    }

    List<ReservationStatus> findAllByStatusInAndReservationMemberId(List<BookStatus> statuses, long memberId);

    @Override
    default boolean existsAlreadyWaitingOrBooked(Reservation reservation) {
        return existsAlreadyWaitingOrBooked(
                reservation.getMember().getId(),
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                NON_CANCELLED_STATUSES
        );
    }

    @Query("""
            select count(rs) > 0 from ReservationStatus rs
            left join rs.reservation r on rs.reservation.id = r.id
            where r.member.id = :id
            and r.theme = :theme
            and r.date = :date
            and r.time = :time
            and rs.status in :statuses
            """)
    boolean existsAlreadyWaitingOrBooked(
            Long id, Theme theme, LocalDate date, ReservationTime time, List<BookStatus> statuses
    );

    @Override
    default List<Reservation> findAllBookedReservations() {
        return findAllByStatus(BookStatus.BOOKED)
                .stream()
                .map(ReservationStatus::getReservation)
                .toList();
    }

    @Override
    default List<Reservation> findAllWaitingReservations() {
        return findAllByStatus(BookStatus.WAITING)
                .stream()
                .map(ReservationStatus::getReservation)
                .toList();
    }

    List<ReservationStatus> findAllByStatus(BookStatus status);

    @Override
    default ReservationStatus getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("예약 정보를 찾을 수 없습니다."));
    }
}
