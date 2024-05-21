package roomescape.application.reservation.fixture;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.domain.role.RoleRepository;

@TestComponent
public class ReservationFixture {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Clock clock;

    public Reservation saveReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = MemberFixture.createMember("아루");
        roleRepository.save(new MemberRole(member, Role.MEMBER));
        Reservation reservation = new Reservation(
                member,
                LocalDate.of(2024, 1, 1),
                time,
                theme,
                LocalDateTime.now(clock)
        );
        return reservationRepository.save(reservation);
    }
}
