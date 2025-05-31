package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.repository.FakeWaitingRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public class FakeWaitingRepositoryFixture {

    public static FakeWaitingRepository create() {
        return new FakeWaitingRepository(List.of(
                new Waiting(LocalDate.now().plusDays(7),
                        new Member(1L, "어드민", "admin@gmail.com", "wooteco7", Role.USER),
                        new Theme(1L, "우테코", "방탈출", "https://"),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        WaitingStatus.PENDING)));
    }
}
