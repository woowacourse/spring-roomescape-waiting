package roomescape.theme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    public void create_success() {
        // when
        Theme theme = themeService.create(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        // then
        assertThat(themeService.list())
                .extracting(Theme::getId)
                .containsExactly(theme.getId());
    }

    @Test
    @DisplayName("이미 존재하는 이름의 테마를 생성하면 예외가 발생한다.")
    public void create_fail() {
        // given
        themeService.create(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        // when, then
        assertThatThrownBy(() -> themeService.create(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        ))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    public void delete_success() {
        // given
        Theme theme = themeService.create(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );

        // when
        themeService.delete(theme.getId());

        // then
        assertThat(themeService.list()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마 삭제를 요청해도 성공한다.")
    public void delete_success_whenThemeNotFound() {
        // when
        themeService.delete(37L);

        // then
        assertThat(themeService.list()).isEmpty();
    }

    @Test
    @DisplayName("예약이 존재하는 테마를 삭제하면 예외가 발생한다.")
    public void delete_fail_whenReservationExists() {
        // given
        Theme theme = themeService.create(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://example.com/theme.png"
        );
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        saveReservation("브라운", LocalDate.now().plusDays(1), time, theme);

        // when, then
        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("지정된 일 수 및 갯수를 기준으로 인기 테마를 조회한다.")
    public void findPopularThemes() {
        // given
        Theme popularTheme = themeRepository.save(new Theme("인기 테마", "인기 테마 설명", "https://example.com/popular.png"));
        Theme lessPopularTheme = themeRepository.save(new Theme("덜 인기 테마", "덜 인기 테마 설명", "https://example.com/less-popular.png"));
        Theme outOfRangeTheme = themeRepository.save(new Theme("기간 밖 테마", "기간 밖 테마 설명", "https://example.com/out-of-range.png"));

        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        LocalDate now = LocalDate.of(2026, 10, 15);
        saveReservation("브라운", LocalDate.of(2026, 10, 8), time, popularTheme);
        saveReservation("레아", LocalDate.of(2026, 10, 8), time2, popularTheme);
        saveReservation("제이슨", LocalDate.of(2026, 10, 9), time, lessPopularTheme);
        saveReservation("포비", now, time, outOfRangeTheme);

        // when
        List<Theme> popularThemes = themeService.findPopularThemes(7, now, 10);

        // then
        assertThat(popularThemes)
                .extracting(Theme::getId)
                .containsExactly(popularTheme.getId(), lessPopularTheme.getId());
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(Reservation.create(
                name,
                date,
                time,
                theme,
                LocalDateTime.of(date, time.getStartAt()).minusMinutes(1)
        ));
    }

}
