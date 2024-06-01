package roomescape.support.fixture;

import org.springframework.stereotype.Component;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationDetailRepository;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

@Component
public class ReservationDetailFixture {

    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationDetailFixture(ReservationDetailRepository reservationDetailRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
    }

    public ReservationDetail createReservationDetail(final LocalDate date, final ReservationTime time, final Theme theme) {
        ReservationDetail reservationDetail = new ReservationDetail(date, time, theme);
        return reservationDetailRepository.save(reservationDetail);
    }
}
