package roomescape.support.fixture;

import java.time.LocalTime;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.reservationtime.ReservationTime;

@TestComponent
public class ReservationTimeFixture extends Fixture {

    public ReservationTime createAndSave() {
        return createAndSave("10:00");
    }

    public ReservationTime createAndSave(String time) {
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse(time));
        em.persist(reservationTime);
        synchronize();
        return reservationTime;
    }
}
