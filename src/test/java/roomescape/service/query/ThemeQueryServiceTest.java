package roomescape.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.repository.JpaThemeRepository;

class ThemeQueryServiceTest {

    @Mock
    private JpaThemeRepository themeRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ThemeQueryService themeQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllThemes() {
        List<Theme> allThemes = List.of(
                new Theme(1L, "공포테마", "무서운 배경 설명", "image-url")
        );

        when(themeRepository.findAll()).thenReturn(allThemes);
        List<ThemeResponseDto> allThemeDtos = themeQueryService.findAllThemes();

        assertThat(allThemeDtos).hasSize(1);
        assertThat(allThemeDtos.get(0)).isEqualTo(new ThemeResponseDto(1L, "공포테마", "무서운 배경 설명", "image-url"));
    }

    @Test
    void findPopularThemes() {
        List<Theme> allThemes = List.of(
                new Theme(1L, "공포테마1", "무서운 배경 설명", "image-url"),
                new Theme(2L, "공포테마2", "무서운 배경 설명", "image-url"),
                new Theme(3L, "공포테마3", "무서운 배경 설명", "image-url"),
                new Theme(4L, "공포테마4", "무서운 배경 설명", "image-url"),
                new Theme(5L, "공포테마5", "무서운 배경 설명", "image-url"),
                new Theme(6L, "공포테마6", "무서운 배경 설명", "image-url"),
                new Theme(7L, "공포테마7", "무서운 배경 설명", "image-url"),
                new Theme(8L, "공포테마8", "무서운 배경 설명", "image-url"),
                new Theme(9L, "공포테마9", "무서운 배경 설명", "image-url"),
                new Theme(10L, "공포테마10", "무서운 배경 설명", "image-url"),
                new Theme(11L, "공포테마11", "무서운 배경 설명", "image-url")
        );

        LocalDate fixedToday = LocalDate.of(2025, 5, 20);
        when(clock.instant()).thenReturn(fixedToday
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(themeRepository.findMostReservedThemesBetweenDate(any(LocalDate.class), any(LocalDate.class))).thenReturn(allThemes);

        List<ThemeResponseDto> popularThemes = themeQueryService.findPopularThemes();

        assertThat(popularThemes).hasSize(10);
        assertThat(popularThemes.getFirst().id()).isEqualTo(1L);
        assertThat(popularThemes.getLast().id()).isEqualTo(10L);
    }
}
