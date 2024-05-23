package roomescape.infra.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.domain.reservationdetail.ReservationDetailRepository;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.Theme;

public interface ReservationDetailJpaRepository extends
        ReservationDetailRepository,
        Repository<ReservationDetail, Long> {

    @Override
    ReservationDetail save(ReservationDetail reservationDetail);

    @Override
    Optional<ReservationDetail> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    @Override
    List<ReservationDetail> findAll();

    @Override
    void delete(ReservationDetail reservationDetail);
}
