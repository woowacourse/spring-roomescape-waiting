package roomescape.testFixture;

import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_2;
import static roomescape.testFixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.mockito.Mockito;
import roomescape.application.MemberService;
import roomescape.application.ThemeService;
import roomescape.application.TimeService;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;

public class StubHelper {

    public static Member stubMember(long memberId, MemberService memberService) {
        Member member = Member.of(memberId, "브라운", "brown@email.com", "brown", Role.USER);
        Mockito.doReturn(member).when(memberService).getMemberEntityById(memberId);
        return member;
    }

    public static Theme stubTheme(long themeId, ThemeService themeService) {
        Theme theme = Theme.of(themeId, "테마1", "테마1입니다.", "썸네일1");
        Mockito.doReturn(theme).when(themeService).getThemeById(themeId);
        return theme;
    }

    public static ReservationTime stubTime(long timeId, TimeService timeService) {
        ReservationTime time = ReservationTime.of(timeId, LocalTime.of(10, 0));
        Mockito.doReturn(time).when(timeService).getTimeEntityById(timeId);
        return time;
    }

    public static List<Reservation> stubReservationsWithStatus(ReservationRepository reservationRepository,
                                                               ReservationStatus status) {
        Reservation reservation1 = Reservation.of(1L, MEMBER1_ADMIN, THEME_1, LocalDate.now(), RESERVATION_TIME_1,
                Status.statusWithoutId(status));
        Reservation reservation2 = Reservation.of(2L, MEMBER1_ADMIN, THEME_1, LocalDate.now(), RESERVATION_TIME_1,
                Status.statusWithoutId(status));
        List<Reservation> reservations = List.of(reservation1, reservation2);
        Mockito.doReturn(reservations)
                .when(reservationRepository)
                .findByStatusStatus(status);
        return reservations;
    }
}
