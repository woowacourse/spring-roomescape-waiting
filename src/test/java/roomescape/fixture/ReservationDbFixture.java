package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.service.out.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Component
public class ReservationDbFixture {

    private final MemberDbFixture memberDbFixture;
    private final ReservationTimeDbFixture reservationTimeDbFixture;
    private final ThemeDbFixture themeDbFixture;
    private final ReservationRepository reservationRepository;

    public ReservationDbFixture(MemberDbFixture memberDbFixture, ReservationTimeDbFixture reservationTimeDbFixture,
                                ThemeDbFixture themeDbFixture,
                                ReservationRepository reservationRepository) {
        this.memberDbFixture = memberDbFixture;
        this.reservationTimeDbFixture = reservationTimeDbFixture;
        this.themeDbFixture = themeDbFixture;
        this.reservationRepository = reservationRepository;
    }

    public Reservation 예약_유저1_내일_10시_공포() {
        Reservation reservation = Reservation.reserve(
                memberDbFixture.유저1_생성(),
                ReservationDateTime.load(
                        ReservationDateFixture.예약날짜_내일,
                        reservationTimeDbFixture.열시()
                ),
                themeDbFixture.공포()
        );
        return reservationRepository.save(reservation);
    }

    public Reservation 예약_유저1(ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        Reservation reservation = Reservation.reserve(
                memberDbFixture.유저1_생성(),
                ReservationDateTime.load(
                        reservationDate,
                        reservationTime
                ),
                theme
        );
        return reservationRepository.save(reservation);
    }

    public Reservation 예약_유저1_내일_11시_공포() {
        Reservation reservation = Reservation.reserve(
                memberDbFixture.유저1_생성(),
                ReservationDateTime.load(
                        ReservationDateFixture.예약날짜_내일,
                        reservationTimeDbFixture.열한시()
                ),
                themeDbFixture.공포()
        );
        return reservationRepository.save(reservation);
    }
}
