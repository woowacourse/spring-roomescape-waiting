package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.Fixture;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Theme theme1 = new Theme("name1", "description1", "thumbnail1");
    private Theme theme2 = new Theme("name2", "description2", "thumbnail2");
    private Theme theme3 = new Theme("name3", "description3", "thumbnail3");
    private Theme theme4 = new Theme("name4", "description4", "thumbnail4");

    private ReservationTime time1 = new ReservationTime(LocalTime.of(1, 0));
    private ReservationTime time2 = new ReservationTime(LocalTime.of(2, 0));
    private ReservationTime time3 = new ReservationTime(LocalTime.of(3, 0));
    private ReservationTime time4 = new ReservationTime(LocalTime.of(4, 0));
    private ReservationTime time5 = new ReservationTime(LocalTime.of(5, 0));

    private Member defaultMember = Fixture.defaultMember;
    private static final LocalDate DAY_BEFORE_1 = LocalDate.now().minusDays(1);
    private static final LocalDate DAY_BEFORE_7 = LocalDate.now().minusDays(7);
    private static final LocalDate DAY_BEFORE_10 = LocalDate.now().minusDays(10);
    @BeforeEach
    void initDate() {
        theme1 = themeRepository.save(theme1);
        theme2 = themeRepository.save(theme2);
        theme3 = themeRepository.save(theme3);
        theme4 = themeRepository.save(theme4);

        time1 = reservationTimeRepository.save(time1);
        time2 = reservationTimeRepository.save(time2);
        time3 = reservationTimeRepository.save(time3);
        time4 = reservationTimeRepository.save(time4);
        time5 = reservationTimeRepository.save(time5);

        defaultMember = memberRepository.save(defaultMember);
    }
    @DisplayName("인기 테마를 구할 수 있다.")
    @Test
    void popularThemeTest() {
        //given
        reservationRepository.save(new Reservation(DAY_BEFORE_1, time1, theme1, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time2, theme1, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time3, theme1, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time4, theme1, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time5, theme1, defaultMember));

        reservationRepository.save(new Reservation(DAY_BEFORE_1, time1, theme2, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_1, time2, theme2, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time3, theme2, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time4, theme2, defaultMember));

        reservationRepository.save(new Reservation(DAY_BEFORE_7, time1, theme3, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time2, theme3, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_7, time3, theme3, defaultMember));

        reservationRepository.save(new Reservation(DAY_BEFORE_10, time1, theme4, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time2, theme4, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time3, theme4, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time4, theme4, defaultMember));
        reservationRepository.save(new Reservation(DAY_BEFORE_10, time5, theme4, defaultMember));

        //when
        List<Theme> popularThemes =
                reservationRepository.findAndOrderByPopularity(DAY_BEFORE_7, DAY_BEFORE_1, 10);

        //then
        assertThat(popularThemes).containsExactly(theme1, theme3, theme2);
    }

}
