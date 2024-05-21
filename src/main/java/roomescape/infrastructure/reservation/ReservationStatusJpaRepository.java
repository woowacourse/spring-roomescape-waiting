package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
        return getFirstByReservationThemeAndReservationDateAndReservationTimeAndStatusOrderByReservationCreatedAtAsc(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                BookStatus.WAITING
        );
    }

    Optional<ReservationStatus> getFirstByReservationThemeAndReservationDateAndReservationTimeAndStatusOrderByReservationCreatedAtAsc(
            Theme theme, LocalDate date, ReservationTime time, BookStatus status
    );

    @Override
    default long getWaitingCount(Reservation reservation) {
        return countByReservationThemeAndReservationDateAndReservationTimeAndStatusInAndReservationCreatedAtLessThan(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                NON_CANCELLED_STATUSES,
                reservation.getCreatedAt()
        );
    }

    long countByReservationThemeAndReservationDateAndReservationTimeAndStatusInAndReservationCreatedAtLessThan(
            Theme theme, LocalDate date, ReservationTime time, List<BookStatus> statuses, LocalDateTime createdAt
    );

    @Override
    default List<ReservationStatus> findActiveReservationStatusesByMemberId(long memberId) {
        return findAllByStatusInAndReservationMemberId(NON_CANCELLED_STATUSES, memberId);
    }

    List<ReservationStatus> findAllByStatusInAndReservationMemberId(List<BookStatus> statuses, long memberId);

    @Override
    default ReservationStatus getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("예약 정보를 찾을 수 없습니다."));
    }
}
