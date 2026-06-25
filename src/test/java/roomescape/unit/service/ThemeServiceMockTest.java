package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.repository.ThemeRepository;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ThemeServiceMockTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void findById는_존재하지_않으면_NotFoundException을_던진다() {
        given(themeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById는_존재하면_테마를_반환한다() {
        Theme theme = new Theme(
                1L,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        assertThat(themeService.findById(1L)).isEqualTo(theme);
    }

    @Test
    void getPopularThemes는_조회_윈도우를_계산해_저장소에_위임한다() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(7);
        LocalDate end = today.minusDays(1);
        given(themeRepository.getPopularThemes(start, end, 10)).willReturn(List.of());

        themeService.getPopularThemes(7, 10);

        verify(themeRepository).getPopularThemes(start, end, 10);
    }
}
