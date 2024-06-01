package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {

    default ReservationDetail getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_DETAIL_NOT_FOUND,
                        String.format("예약 정보(ReservationDetail)가 존재하지 않습니다. [reservationDetailId: %d]", id)));
    }

    List<ReservationDetail> findByReservationTime(ReservationTime reservationTime);

    Optional<ReservationDetail> findByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);

    List<ReservationDetail> findByDateAndTheme(LocalDate date, Theme theme);
}
