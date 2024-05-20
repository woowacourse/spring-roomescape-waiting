package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ThemeDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql("/init.sql")
@SpringBootTest
class ThemeServiceTest {

    private static final int INITIAL_THEME_COUNT = 3;

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.now().minusDays(1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Reservation(
                        LocalDate.now().minusDays(8),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN))));
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        List<Theme> themes = themeService.findAllThemes();
        assertThat(themes).hasSize(INITIAL_THEME_COUNT);
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void should_save_theme() {
        ThemeDto themeDto = new ThemeDto("n4", "d4", "t4");
        themeService.saveTheme(themeDto);
        assertThat(themeService.findAllThemes()).hasSize(INITIAL_THEME_COUNT + 1);
    }

    @DisplayName("중복된 이름의 테마를 저장하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_duplicated_name() {
        ThemeDto themeDto = new ThemeDto("n1", "d", "t");
        assertThatThrownBy(() -> themeService.saveTheme(themeDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 테마의 이름은 중복될 수 없습니다.");
    }

    @DisplayName("예약이 존재하지 않는 테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        themeService.deleteTheme(3L);
        assertThat(themeService.findAllThemes()).hasSize(INITIAL_THEME_COUNT - 1);
    }

    @DisplayName("예약이 존재하는 테마를 삭제하려고 할 경우 예외가 발생한다..")
    @Test
    void should_throw_exception_when_reservation_exist() {
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 해당 테마를 사용하고 있는 예약이 있습니다.");
    }

    @DisplayName("존재하지 않는 테마를 삭제하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 테마입니다.");
    }

    @DisplayName("최근 일주일 간 가장 인기 있는 테마 10개를 조회한다.")
    @Test
    void should_find_popular_theme_of_week() {
        List<Theme> popularThemes = themeService.findPopularThemes();
        assertThat(popularThemes).hasSize(2);
    }
}