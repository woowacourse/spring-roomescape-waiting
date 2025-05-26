package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.infrastructure.repository.ReservationRepository;

import java.time.LocalDate;

@Component
public class ReservationDbFixture {

    private final ReservationRepository reservationRepository;

    public ReservationDbFixture(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation 예약_한스_25_4_22_10시_공포(Member member, ReservationTime reservationTime, Theme theme) {
        LocalDate date = ReservationDateFixture.예약날짜_25_4_22.getDate();
        Reservation reservation = Reservation.create(member, date, reservationTime, theme);

        return reservationRepository.save(reservation);
    }

    public Reservation 예약_생성(Member member, ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        LocalDate date = reservationDate.getDate();
        Reservation reservation = Reservation.create(member, date, reservationTime, theme);

        return reservationRepository.save(reservation);
    }
}
