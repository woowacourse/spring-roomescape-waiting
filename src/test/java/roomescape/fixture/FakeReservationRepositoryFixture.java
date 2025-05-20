package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.repository.FakeReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class FakeReservationRepositoryFixture {

    public static FakeReservationRepository create() {
        return new FakeReservationRepository(List.of(
                new Reservation(1L, new Member(1L, "어드민", "admin@gmail.com", "wooteco7", Role.USER),
                        LocalDate.now().plusDays(7),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "우테코", "방탈출", "https://"),
                        ReservationStatus.RESERVED
                )
        ));
    }
}
