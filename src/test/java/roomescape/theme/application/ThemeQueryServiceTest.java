package roomescape.theme.application;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class ThemeQueryServiceTest {

    @Autowired
    private ThemeQueryService themeQueryService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Test
    @DisplayName("테마를 모두 조회할 수 있다")
    void getAll() {
        // given
        final String name1 = "시소";
        final String description1 = "공포 방탈출 대표 테마";
        final String url1 = "https://www.naver.com";

        final Theme saved1 = themeRepository.save(Theme.withoutId(
                ThemeName.from(name1),
                ThemeDescription.from(description1),
                ThemeThumbnail.from(url1)));

        final String name2 = "강산";
        final String description2 = "유머 방탈출 대표 테마";
        final String url2 = "https://www.daum.com";

        final Theme saved2 = themeRepository.save(Theme.withoutId(
                ThemeName.from(name2),
                ThemeDescription.from(description2),
                ThemeThumbnail.from(url2)));

        // when
        final List<Theme> themes = themeQueryService.getAll();

        // then
        assertThat(themes).hasSize(2);
        assertThat(themes).contains(saved1, saved2);
    }

    @Test
    @DisplayName("주어진 기간동안 예약 수가 많은 상위 5개 테마를 조회할 수 있다")
    void getTopThemeRanking() {
        // given
        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL
                )
        );

        final Theme[] themes = new Theme[6];
        for (int i = 0; i < 6; i++) {
            themes[i] = themeRepository.save(
                    Theme.withoutId(
                            ThemeName.from("테마" + (i + 1)),
                            ThemeDescription.from("설명" + (i + 1)),
                            ThemeThumbnail.from("https://example.com/" + (i + 1))
                    )
            );
        }

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        final TimeSlot time = timeSlotRepository.save(
                TimeSlot.withoutId(ReservationTime.from(LocalTime.of(12, 0)))
        );

        saveReservations(themes[0], user, date, time, 20);
        saveReservations(themes[1], user, date, time, 10);
        saveReservations(themes[2], user, date, time, 8);
        saveReservations(themes[3], user, date, time, 6);
        saveReservations(themes[4], user, date, time, 3);
        saveReservations(themes[5], user, date, time, 1);

        // when
        final int rankingCount = 5;
        final List<Theme> rankedThemes = themeQueryService.getRanking(
                date,
                ReservationDate.from(date.getValue().plusDays(30)),
                rankingCount
        );

        // then
        assertAll(
                () -> assertThat(rankedThemes).hasSize(rankingCount),
                () -> assertThat(rankedThemes)
                        .containsExactly(themes[0], themes[1], themes[2], themes[3], themes[4])
        );
    }

    private void saveReservations(final Theme theme,
                                  final User user,
                                  final ReservationDate date,
                                  final TimeSlot time,
                                  final int count) {
        for (int i = 0; i < count; i++) {
            reservationRepository.save(
                    Reservation.withoutId(
                            user.getId(),
                            ReservationDate.from(date.getValue().plusDays(i)),
                            time.getStartAt(),
                            theme
                    )
            );
        }
    }
}
