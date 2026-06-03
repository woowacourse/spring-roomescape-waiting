package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.RoomEscapeException;
import roomescape.theme.application.dto.PopularThemeQueryResult;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.PopularTheme;
import roomescape.theme.domain.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeService themeService;

    private final Theme theme = Theme.builder()
            .id(1L).name("theme name").description("theme description").thumbnailImgUrl("theme img url")
            .build();

    @DisplayName("테마의 정상 추가를 테스트합니다.")
    @Test
    void save_theme_successfully() {
        when(themeRepository.existsByNameAndDescription(any())).thenReturn(false);
        when(themeRepository.save(any())).thenReturn(theme);

        ThemeQueryResult result = themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.name()).isEqualTo("theme name");
            softly.assertThat(result.description()).isEqualTo("theme description");
            softly.assertThat(result.thumbnailImgUrl()).isEqualTo("theme img url");
        });
    }

    @DisplayName("중복된 테마 추가 시 예외 발생을 테스트합니다.")
    @Test
    void save_duplicated_theme_exception() {
        when(themeRepository.existsByNameAndDescription(any())).thenReturn(true);

        assertThatThrownBy(() -> themeService.save(new ThemeCreateCommand("theme name", "theme description", "theme img url")))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름과 설명이 같은 테마가 이미 존재합니다.");
    }

    @DisplayName("테마의 삭제를 테스트합니다.")
    @Test
    void delete_theme() {
        when(themeRepository.delete(1L)).thenReturn(1);

        assertThat(themeService.delete(1L)).isEqualTo(1);
    }

    @DisplayName("테마 조회를 테스트합니다.")
    @Test
    void find_theme() {
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

        ThemeQueryResult result = themeService.findById(1L);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(1L);
            softly.assertThat(result.name()).isEqualTo("theme name");
            softly.assertThat(result.description()).isEqualTo("theme description");
            softly.assertThat(result.thumbnailImgUrl()).isEqualTo("theme img url");
        });
    }

    @DisplayName("존재하지 않는 테마 조회 시 예외 발생을 테스트합니다.")
    @Test
    void theme_not_exists() {
        when(themeRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.findById(100L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("존재하지 않는 테마 입니다.");
    }

    @DisplayName("테마의 전체 조회를 테스트합니다.")
    @Test
    void find_all_themes() {
        List<Theme> themes = List.of(
                Theme.builder().id(1L).name("theme name1").description("theme description1").thumbnailImgUrl("theme img url1").build(),
                Theme.builder().id(2L).name("theme name2").description("theme description2").thumbnailImgUrl("theme img url2").build(),
                Theme.builder().id(3L).name("theme name3").description("theme description3").thumbnailImgUrl("theme img url3").build()
        );
        when(themeRepository.findAll()).thenReturn(themes);

        List<ThemeQueryResult> result = themeService.findAll();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result).containsExactly(
                    new ThemeQueryResult(1L, "theme name1", "theme description1", "theme img url1"),
                    new ThemeQueryResult(2L, "theme name2", "theme description2", "theme img url2"),
                    new ThemeQueryResult(3L, "theme name3", "theme description3", "theme img url3")
            );
        });
    }

    @DisplayName("인기 테마 조회를 테스트합니다.")
    @Test
    void find_popular_themes() {
        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(themeRepository.findTop10PopularThemesBetween(any(), any())).thenReturn(List.of(
                new PopularTheme(1L, "theme name", "theme description", "theme img url", 10)
        ));

        List<PopularThemeQueryResult> result = themeService.findPopularThemes(LocalDate.of(2026, 5, 6));

        verify(themeRepository).findTop10PopularThemesBetween(fromCaptor.capture(), toCaptor.capture());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(fromCaptor.getValue()).isEqualTo(LocalDate.of(2026, 4, 29));
            softly.assertThat(toCaptor.getValue()).isEqualTo(LocalDate.of(2026, 5, 5));
            softly.assertThat(result).containsExactly(
                    new PopularThemeQueryResult(1L, "theme name", "theme description", "theme img url", 10)
            );
        });
    }
}
