package roomescape.fixture;

import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationFixture {

    public static Reservation createWaiting() {
        return new Reservation(
            6L,
            new Member(2L, "사용자", "user@a.com", "123a!", Role.USER),
            LocalDate.now().plusDays(1).toString(),
            new ReservationTime(1L, "10:00"),
            new Theme(1L, "theme1", "desc1", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
            ReservationStatus.WAITING
        );
    }
}
