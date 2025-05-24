package roomescape.integration.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationSchedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationRepository;

@Component
public class ReservationDbFixture {

    private final ReservationRepository reservationRepository;

    public ReservationDbFixture(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation 예약_25_4_22(final ReservationTime time, final Theme theme, final Member member) {
        ReservationDate date = ReservationDateFixture.예약날짜_25_4_22;
        return createReservation(date, time, theme, member);
    }

    public Reservation 예약_생성(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        return createReservation(date, time, theme, member);
    }

    public Reservation createReservation(
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        return reservationRepository.save(new Reservation(null, member, new ReservationSchedule(date, time, theme)));
    }
}
