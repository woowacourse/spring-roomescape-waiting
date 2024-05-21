package roomescape.config;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Reservation;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberCommandRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.TimeCommandRepository;
import roomescape.domain.repository.WaitingCommandRepository;

@Component
@Profile("test")
public class TestDataInitializer implements CommandLineRunner {

    private final ReservationCommandRepository reservationCommandRepository;
    private final MemberCommandRepository memberCommandRepository;
    private final TimeCommandRepository timeCommandRepository;
    private final ThemeCommandRepository themeCommandRepository;
    private final WaitingCommandRepository waitingCommandRepository;

    public TestDataInitializer(ReservationCommandRepository reservationCommandRepository,
                               MemberCommandRepository memberCommandRepository,
                               TimeCommandRepository timeCommandRepository,
                               ThemeCommandRepository themeCommandRepository,
                               WaitingCommandRepository waitingCommandRepository) {
        this.reservationCommandRepository = reservationCommandRepository;
        this.memberCommandRepository = memberCommandRepository;
        this.timeCommandRepository = timeCommandRepository;
        this.themeCommandRepository = themeCommandRepository;
        this.waitingCommandRepository = waitingCommandRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Member member1 = createMember("피케이", "pk@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member2 = createMember("이상", "ls@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member3 = createMember("회원", "member@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member4 = createMember("운영자", "admin@wooteco.com", "wootecoCrew6!", Role.ADMIN);

        Time time1 = createTime("10:00");
        Time time2 = createTime("11:00");
        Time time3 = createTime("12:00");
        Time time4 = createTime("13:00");

        Theme theme1 = createTheme("테마1", "테마1 설명");
        Theme theme2 = createTheme("테마2", "테마2 설명");
        Theme theme3 = createTheme("테마3", "테마3 설명");

        createReservation(member1, LocalDate.of(1999, 12, 25), time1, theme1);
        createReservation(member1, LocalDate.now().minusDays(1), time1, theme1);
        createReservation(member2, LocalDate.now().minusDays(2), time1, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), time2, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), time3, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), time4, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), time1, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), time2, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), time3, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), time4, theme2);
        createReservation(member4, LocalDate.now().minusDays(4), time1, theme3);
        createReservation(member4, LocalDate.now().minusDays(4), time2, theme3);
        createReservation(member4, LocalDate.now().minusDays(4), time3, theme3);

        LocalDate date = LocalDate.now().plusDays(1);
        createReservation(member1, date, time1, theme1);
        createReservation(member2, date, time1, theme2);
        createReservation(member3, date, time1, theme3);
    }

    private Member createMember(String name, String email, String password, Role role) {
        return memberCommandRepository.save(
                new Member(new PlayerName(name), new Email(email), new Password(password), role));
    }

    private Time createTime(String time) {
        return timeCommandRepository.save(new Time(LocalTime.parse(time)));
    }

    private Theme createTheme(String name, String description) {
        return themeCommandRepository.save(new Theme(new ThemeName(name), description, "https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png"));
    }

    private Reservation createReservation(Member member, LocalDate date, Time time, Theme theme) {
        return reservationCommandRepository.save(new Reservation(member, date, time, theme));
    }

    private Waiting createWaiting(Member member, LocalDate date, Time time, Theme theme) {
        return waitingCommandRepository.save(new Waiting(member, date, time, theme));
    }
}
