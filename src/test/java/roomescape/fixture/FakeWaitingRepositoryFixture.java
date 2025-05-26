package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.repository.FakeWaitingRepository;
import roomescape.reservation.domain.ReservationDetails;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class FakeWaitingRepositoryFixture {

    public static FakeWaitingRepository create() {
        return new FakeWaitingRepository(List.of(
                new Waiting(1L, new Member(1L, "어드민", "admin@gmail.com", "wooteco7", Role.USER),
                        new ReservationDetails(
                                LocalDate.now().plusDays(7),
                                new ReservationTime(1L, LocalTime.of(10, 0)),
                                new Theme(1L, "우테코", "방탈출", "https://")
                        )
                )
        ));
    }
}
