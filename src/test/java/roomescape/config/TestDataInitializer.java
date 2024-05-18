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
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;
import roomescape.domain.repository.MemberCommandRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationTimeCommandRepository;
import roomescape.domain.repository.ThemeCommandRepository;

@Component
@Profile("test")
public class TestDataInitializer implements CommandLineRunner {

    private final ReservationCommandRepository reservationCommandRepository;
    private final MemberCommandRepository memberCommandRepository;
    private final ReservationTimeCommandRepository reservationTimeCommandRepository;
    private final ThemeCommandRepository themeCommandRepository;

    public TestDataInitializer(ReservationCommandRepository reservationCommandRepository,
                               MemberCommandRepository memberCommandRepository,
                               ReservationTimeCommandRepository reservationTimeCommandRepository,
                               ThemeCommandRepository themeCommandRepository) {
        this.reservationCommandRepository = reservationCommandRepository;
        this.memberCommandRepository = memberCommandRepository;
        this.reservationTimeCommandRepository = reservationTimeCommandRepository;
        this.themeCommandRepository = themeCommandRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Member member1 = createMember("피케이", "pk@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member2 = createMember("이상", "ls@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member3 = createMember("회원", "member@wooteco.com", "wootecoCrew6!", Role.BASIC);
        Member member4 = createMember("운영자", "admin@wooteco.com", "wootecoCrew6!", Role.ADMIN);

        ReservationTime reservationTime1 = createReservationTime("10:00");
        ReservationTime reservationTime2 = createReservationTime("11:00");
        ReservationTime reservationTime3 = createReservationTime("12:00");
        ReservationTime reservationTime4 = createReservationTime("13:00");

        Theme theme1 = createTheme("테마1", "테마1 설명");
        Theme theme2 = createTheme("테마2", "테마2 설명");
        Theme theme3 = createTheme("테마3", "테마3 설명");

        createReservation(member1, LocalDate.now().minusDays(1), reservationTime1, theme1);
        createReservation(member2, LocalDate.now().minusDays(2), reservationTime1, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), reservationTime2, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), reservationTime3, theme2);
        createReservation(member2, LocalDate.now().minusDays(2), reservationTime4, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), reservationTime1, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), reservationTime2, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), reservationTime3, theme2);
        createReservation(member3, LocalDate.now().minusDays(3), reservationTime4, theme2);
        createReservation(member4, LocalDate.now().minusDays(4), reservationTime1, theme3);
        createReservation(member4, LocalDate.now().minusDays(4), reservationTime2, theme3);
        createReservation(member4, LocalDate.now().minusDays(4), reservationTime3, theme3);
    }

    private Member createMember(String name, String email, String password, Role role) {
        return memberCommandRepository.save(
                new Member(new PlayerName(name), new Email(email), new Password(password), role));
    }

    private ReservationTime createReservationTime(String time) {
        return reservationTimeCommandRepository.save(new ReservationTime(LocalTime.parse(time)));
    }

    private Theme createTheme(String name, String description) {
        return themeCommandRepository.save(new Theme(new ThemeName(name), description, "https://upload.wikimedia.org/wikipedia/en/thumb/3/3b/SpongeBob_SquarePants_character.svg/440px-SpongeBob_SquarePants_character.svg.png"));
    }

    private Reservation createReservation(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        return reservationCommandRepository.save(new Reservation(member, date, reservationTime, theme));
    }
}
