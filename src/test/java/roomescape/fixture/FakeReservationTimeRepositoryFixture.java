package roomescape.fixture;

import java.time.LocalTime;
import java.util.List;
import roomescape.domain.ReservationTime;
import roomescape.repository.FakeReservationTimeRepository;

public class FakeReservationTimeRepositoryFixture {

    public static FakeReservationTimeRepository create() {
        return new FakeReservationTimeRepository(List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(13, 0))
        ));
    }
}
