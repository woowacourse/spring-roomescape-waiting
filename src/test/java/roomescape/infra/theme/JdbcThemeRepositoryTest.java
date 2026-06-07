package roomescape.infra.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.domain.user.User;
import roomescape.infra.reservation.JdbcReservationRepository;
import roomescape.infra.reservation.JdbcReservationSlotRepository;
import roomescape.infra.reservation.JdbcReservationTimeRepository;
import roomescape.infra.user.JdbcUserRepository;

@DisplayName("테마 JDBC 저장소")
@JdbcTest(properties = "spring.sql.init.mode=always")
@Import({
        JdbcUserRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationSlotRepository.class,
        JdbcReservationRepository.class,
        JdbcThemeRepository.class
})
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @Autowired
    private JdbcReservationSlotRepository slotRepository;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @DisplayName("테마를 저장할 수 있다")
    @Test
    void save() {
        // given
        Theme saved = themeRepository.save(Theme.create("심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea"));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("심해 공포");
        assertThat(saved.getDescription()).isEqualTo("심해 탈출 공포 테마");
        assertThat(saved.getThumbnailUrl()).isEqualTo("/themes/deep-sea");
    }

    @DisplayName("테마 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // when & then
        assertThat(themeRepository.findAll())
                .extracting(Theme::getName)
                .contains("공포");
    }

    @DisplayName("인기 테마를 조회할 수 있다")
    @Test
    void findPopularThemes() {
        // given
        Theme popularTheme = themeRepository.save(Theme.create("도심 추격전", "도심에서 벌어지는 추격 테마", "/themes/chase"));
        Theme lessPopularTheme = themeRepository.save(Theme.create("고대 유적 탐험", "고대 유적을 탐험하는 테마", "/themes/ruins"));
        ReservationTime time = timeRepository.save(ReservationTime.create(LocalTime.of(9, 0)));

        ReservationSlot popularSlot = slotRepository.save(
                ReservationSlot.create(LocalDate.of(2030, 1, 1), time, popularTheme)
        );
        ReservationSlot lessPopularSlot = slotRepository.save(
                ReservationSlot.create(LocalDate.of(2030, 1, 2), time, lessPopularTheme)
        );

        reservationRepository.save(Reservation.create(
                userRepository.save(User.create("테스트홍길동")),
                popularSlot,
                LocalDateTime.of(2030, 1, 1, 9, 0)
        ));
        reservationRepository.save(Reservation.create(
                userRepository.save(User.create("테스트김철수")),
                popularSlot,
                LocalDateTime.of(2030, 1, 1, 9, 5)
        ));
        reservationRepository.save(Reservation.create(
                userRepository.save(User.create("테스트이영희")),
                popularSlot,
                LocalDateTime.of(2030, 1, 1, 9, 10)
        ));
        reservationRepository.save(Reservation.create(
                userRepository.save(User.create("테스트박민수")),
                lessPopularSlot,
                LocalDateTime.of(2030, 1, 2, 9, 0)
        ));

        // when
        List<ThemeRankResult> results = themeRepository.findPopularThemes(
                10,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 31)
        );

        // then
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.getFirst().name()).isEqualTo("도심 추격전");
        assertThat(results.getFirst().rank()).isEqualTo(1);
        assertThat(results).extracting(ThemeRankResult::name)
                .contains("도심 추격전", "고대 유적 탐험");
    }
}
