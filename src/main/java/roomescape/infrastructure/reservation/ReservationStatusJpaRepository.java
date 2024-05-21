package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    default Optional<ReservationStatus> findFirstWaitingBy(Theme theme, LocalDate date, ReservationTime time) {
        return getFirstByReservationThemeAndReservationDateAndReservationTimeAndStatusOrderByReservationCreatedAtAsc(
                theme, date, time, BookStatus.WAITING
        );
    }

    Optional<ReservationStatus> getFirstByReservationThemeAndReservationDateAndReservationTimeAndStatusOrderByReservationCreatedAtAsc(
            Theme theme, LocalDate date, ReservationTime time, BookStatus status
    );

    @Override
    default long getWaitingCount(Reservation reservation) {
        return countByReservationThemeAndReservationDateAndReservationTimeAndReservationCreatedAtLessThan(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getCreatedAt()
        );
    }

    long countByReservationThemeAndReservationDateAndReservationTimeAndReservationCreatedAtLessThan(
            Theme theme, LocalDate date, ReservationTime time, LocalDateTime createdAt
    );

    @Override
    default ReservationStatus getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("예약 정보를 찾을 수 없습니다."));
    }
}
