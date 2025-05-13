package roomescape.theme.application;

import org.assertj.core.api.SoftAssertions;
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
import roomescape.theme.application.service.ThemeQueryServiceImpl;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ThemeQueryServiceImplTest {

    @Autowired
    private ThemeQueryServiceImpl themeQueryService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private UserRepository userRepository;

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
    @DisplayName("주어진 기간동안에 상위 10개의 테마를 조회할 수 있다")
    void getRanking() {
        // given
        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final Theme theme1 = themeRepository.save(Theme.withoutId(
                ThemeName.from("시소"),
                ThemeDescription.from("공포 방탈출 대표 테마"),
                ThemeThumbnail.from("https://www.naver.com")));

        final Theme theme2 = themeRepository.save(Theme.withoutId(
                ThemeName.from("강산"),
                ThemeDescription.from("유머 방탈출 대표 테마"),
                ThemeThumbnail.from("https://www.daum.net")));

        final Theme theme3 = themeRepository.save(Theme.withoutId(
                ThemeName.from("미로"),
                ThemeDescription.from("추리 방탈출 대표 테마"),
                ThemeThumbnail.from("https://example.com/maze.jpg")));

        final Theme theme4 = themeRepository.save(Theme.withoutId(
                ThemeName.from("엘리베이터"),
                ThemeDescription.from("스릴 넘치는 테마"),
                ThemeThumbnail.from("https://example.com/elevator.jpg")));

        final Theme theme5 = themeRepository.save(Theme.withoutId(
                ThemeName.from("지하실"),
                ThemeDescription.from("공포의 지하실 탈출"),
                ThemeThumbnail.from("https://example.com/basement.jpg")));

        final Theme theme6 = themeRepository.save(Theme.withoutId(
                ThemeName.from("도서관"),
                ThemeDescription.from("지식으로 푸는 방탈출"),
                ThemeThumbnail.from("https://example.com/library.jpg")));

        final Theme theme7 = themeRepository.save(Theme.withoutId(
                ThemeName.from("우주선"),
                ThemeDescription.from("SF 우주 방탈출"),
                ThemeThumbnail.from("https://example.com/spaceship.jpg")));

        final Theme theme8 = themeRepository.save(Theme.withoutId(
                ThemeName.from("놀이공원"),
                ThemeDescription.from("가족용 테마"),
                ThemeThumbnail.from("https://example.com/themepark.jpg")));

        final Theme theme9 = themeRepository.save(Theme.withoutId(
                ThemeName.from("동굴"),
                ThemeDescription.from("자연 탐험 테마"),
                ThemeThumbnail.from("https://example.com/cave.jpg")));

        final Theme theme10 = themeRepository.save(Theme.withoutId(
                ThemeName.from("병원"),
                ThemeDescription.from("공포병원 탈출"),
                ThemeThumbnail.from("https://example.com/hospital.jpg")));

        final Theme theme11 = themeRepository.save(Theme.withoutId(
                ThemeName.from("사무실"),
                ThemeDescription.from("회사에서 탈출하라!"),
                ThemeThumbnail.from("https://example.com/office.jpg")));

        final Theme theme12 = themeRepository.save(Theme.withoutId(
                ThemeName.from("열차"),
                ThemeDescription.from("움직이는 열차 안에서의 탈출"),
                ThemeThumbnail.from("https://example.com/train.jpg")));

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1L));
        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 0)));

        final Theme[] themes = {
                theme1, theme2, theme3, theme4, theme5, theme6,
                theme7, theme8, theme9, theme10, theme11, theme12
        };

        for (int i = 0; i < themes.length; i++) {
            if (i == 3) {
                for (int j = 0; j < 20; j++) {
                    reservationRepository.save(Reservation.withoutId(
                            user.getId(),
                            ReservationDate.from(date.getValue().plusDays(j)),
                            time,
                            themes[i]));
                }
            } else {
                for (int j = 0; j < 11 - i; j++) {
                    reservationRepository.save(Reservation.withoutId(
                            user.getId(),
                            ReservationDate.from(date.getValue().plusDays(j)),
                            time,
                            themes[i]
                    ));
                }
            }
        }

        // when
        final int count = 10;
        final List<Theme> rankedThemes = themeQueryService.getRanking(
                date,
                ReservationDate.from(date.getValue().plusDays(30)),
                count);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(rankedThemes)
                    .hasSize(count);
            softAssertions.assertThat(rankedThemes.getFirst())
                    .isEqualTo(theme4);

            final List<Theme> expectedOrder =
                    List.of(theme4, theme1, theme2, theme3, theme5, theme6, theme7, theme8, theme9, theme10);
            softAssertions.assertThat(rankedThemes)
                    .containsExactlyElementsOf(expectedOrder);
        });

    }
}
