package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationName;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.FakeReservationRepository;

public class FakeReservationRepositoryFixture {

    public static FakeReservationRepository create() {
        return new FakeReservationRepository(List.of(
                new Reservation(1L, new ReservationName(1L, "어드민"), LocalDate.now().plusDays(7),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "우테코", "방탈출", "https://"))
        ));
    }
}
