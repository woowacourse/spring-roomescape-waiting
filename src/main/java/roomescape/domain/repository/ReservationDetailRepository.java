package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.ReservationDetail;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationDetailRepository extends Repository<ReservationDetail, Long> {
    ReservationDetail save(ReservationDetail reservationDetail);

    default ReservationDetail getByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        return findByDateAndTimeAndTheme(date, time, theme)
                .orElseGet(() -> save(new ReservationDetail(date, time, theme)));
    }

    Optional<ReservationDetail> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<ReservationDetail> findAll();

    void delete(ReservationDetail reservationDetail);
}
