package roomescape.service;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Password;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class FixtureUsingTest {
    @Autowired
    protected ReservationRepository reservationRepository;
    @Autowired
    protected ReservationTimeRepository reservationTimeRepository;
    @Autowired
    protected ReservationTimeService reservationTimeService;
    @Autowired
    protected ThemeRepository themeRepository;
    @Autowired
    protected MemberRepository memberRepository;

    protected Member ADMIN = new Member(new Name("ADMIN"), Role.ADMIN, new Email("email@email.com"),
            new Password("password"));
    protected Member USER = new Member(new Name("USER"), Role.USER, new Email("user@user.com"),
            new Password("password"));

    protected long reservationTimeIdNotExists;
    protected int countOfSavedReservationTime = 4;
    protected static ReservationTime reservationTimeNotSaved = new ReservationTime(LocalTime.of(9, 0));
    protected static ReservationTime reservationTime_10_0 = new ReservationTime(LocalTime.of(10, 0));
    protected static ReservationTime reservationTime_11_0 = new ReservationTime(LocalTime.of(11, 0));
    protected static ReservationTime reservationTime_12_0 = new ReservationTime(LocalTime.of(12, 0));
    protected static ReservationTime reservationTime_13_0 = new ReservationTime(LocalTime.of(13, 0));

    protected long themeIdNotSaved;
    protected int countOfSavedTheme = 5;
    protected static Theme theme1 = new Theme("name1", "description1", "thumbnail1");
    protected static Theme theme2 = new Theme("name2", "description2", "thumbnail2");
    protected static Theme theme3 = new Theme("name3", "description3", "thumbnail3");
    protected static Theme theme4 = new Theme("name4", "description4", "thumbnail4");
    protected static Theme theme5 = new Theme("name5", "description5", "thumbnail5");

    @BeforeEach
    void saveDefaultData() {
        ADMIN = memberRepository.save(ADMIN);
        USER = memberRepository.save(USER);

        reservationTime_10_0 = reservationTimeRepository.save(reservationTime_10_0);
        reservationTime_11_0 = reservationTimeRepository.save(reservationTime_11_0);
        reservationTime_12_0 = reservationTimeRepository.save(reservationTime_12_0);
        reservationTime_13_0 = reservationTimeRepository.save(reservationTime_13_0);
        reservationTimeIdNotExists = reservationTime_13_0.getId() + 1;

        theme1 = themeRepository.save(theme1);
        theme2 = themeRepository.save(theme2);
        theme3 = themeRepository.save(theme3);
        theme4 = themeRepository.save(theme4);
        theme5 = themeRepository.save(theme5);
        themeIdNotSaved = theme5.getId() + 1;
    }
}
