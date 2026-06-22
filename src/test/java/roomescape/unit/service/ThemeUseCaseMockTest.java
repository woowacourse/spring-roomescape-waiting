package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.query.ThemeQueryService;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ThemeUseCaseMockTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeQueryService themeQueryService;

    @Test
    void getById는_존재하지_않으면_NotFoundException을_던진다() {
        given(themeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> themeQueryService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById는_존재하면_테마를_반환한다() {
        Theme theme = new Theme(1L, "공포", "무서운 테마", "https://example.com/horror.jpg");
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        assertThat(themeQueryService.getById(1L)).isEqualTo(theme);
    }

    @Test
    void findPopular는_조회_윈도우를_계산해_저장소에_위임한다() {
        LocalDate now = LocalDate.of(2026, 5, 8);
        given(themeRepository.findPopularThemes(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10))
                .willReturn(List.of());

        themeQueryService.findPopular(now, 7, 10);

        verify(themeRepository).findPopularThemes(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);
    }
}
