package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.controller.reservation.dto.PopularThemeResponse;
import roomescape.domain.Theme;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.service.exception.ThemeUsedException;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;

    @Test
    @DisplayName("예약이 있는 테마를 삭제할 경우 예외가 발생한다.")
    void invalidDelete() {
        assertThatThrownBy(() -> themeService.deleteTheme(2L))
                .isInstanceOf(ThemeUsedException.class);
    }

    @Test
    @DisplayName("유명 테마 검색")
    void findPopularTheme() {
        //given
        final LocalDate now = LocalDate.now();
        final List<PopularThemeResponse> expected = List.of(
                PopularThemeResponse.from(new Theme(2L, "여름", "설명2", null)),
                PopularThemeResponse.from(new Theme(1L, "봄", "설명1", null)),
                PopularThemeResponse.from(new Theme(3L, "가을", "설명3", null))
        );
        //when
        final List<PopularThemeResponse> popularThemes = themeService
                .findMostBookedThemesBetweenLimited(
                        now.minusDays(8), now.minusDays(1), 10);
        //then
        assertThat(popularThemes).isEqualTo(expected);
    }

    @Test
    @DisplayName("인기 테마 조회시 from이 until보다 앞일 경우 예외가 발생한다.")
    void invalidPopularDate() {
        final LocalDate now = LocalDate.now();
        assertThatThrownBy(() -> themeService.findMostBookedThemesBetweenLimited(now.minusDays(1),
                now.minusDays(8), 10))
                .isInstanceOf(InvalidRequestException.class);
    }
}
