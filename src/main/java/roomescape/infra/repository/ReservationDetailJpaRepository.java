package roomescape.infra.repository;

import java.time.LocalDate;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.domain.reservationdetail.ReservationDetailRepository;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.Theme;

public interface ReservationDetailJpaRepository extends
        ReservationDetailRepository,
        Repository<ReservationDetail, Long> {

    @Override
    default ReservationDetail getByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(new ReservationDetail(date, time, theme)));
    }
}
