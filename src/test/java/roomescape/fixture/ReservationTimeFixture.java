package roomescape.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.ReservationTime;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.output.ReservationTimeOutput;

@Component
public class ReservationTimeFixture {
    @Autowired
    private ReservationTimeService reservationTimeService;

    public ReservationTimeOutput 예약_시간_생성() {
        return reservationTimeService.createReservationTime(reservationTimeInput());
    }

    public static ReservationTime getDomain() {
        return ReservationTime.from("11:00");
    }

    public static ReservationTimeInput reservationTimeInput() {
        return new ReservationTimeInput("10:00");
    }
}
