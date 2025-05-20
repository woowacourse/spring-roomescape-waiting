package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    private Theme testTheme;
    private ThemeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testTheme = new Theme(1L, "Test Theme", "Test Description", "test-thumbnail.jpg");
        createRequest = new ThemeCreateRequest("Test Theme", "Test Description", "test-thumbnail.jpg");
    }

    @Test
    @DisplayName("새로운 테마를 생성할 수 있다")
    void createTheme_WithValidRequest_ReturnsThemeResponse() {
        // given
        when(themeRepository.save(any(Theme.class))).thenReturn(testTheme);

        // when
        ThemeResponse response = themeService.createTheme(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testTheme.getId());
        assertThat(response.name()).isEqualTo(testTheme.getName());
        assertThat(response.description()).isEqualTo(testTheme.getDescription());
        assertThat(response.thumbnail()).isEqualTo(testTheme.getThumbnail());
    }

    @Test
    @DisplayName("중복된 이름으로 테마를 생성하면 예외가 발생한다")
    void createTheme_WithDuplicateName_ThrowsDuplicateContentException() {
        // given
        when(themeRepository.save(any(Theme.class)))
                .thenThrow(new IllegalStateException("중복된 테마 이름입니다"));

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(createRequest))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("중복된 테마 이름입니다");
    }

    @Test
    @DisplayName("모든 테마를 조회할 수 있다")
    void findAllThemes_ReturnsAllThemes() {
        // given
        Theme theme1 = testTheme;
        Theme theme2 = new Theme(2L, "Another Theme", "Another Description", "another-thumbnail.jpg");
        List<Theme> themes = Arrays.asList(theme1, theme2);

        when(themeRepository.findAll()).thenReturn(themes);

        // when
        List<ThemeResponse> responses = themeService.findAllThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("Test Theme");
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).name()).isEqualTo("Another Theme");
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다")
    void deleteThemeById_WithExistingId_DeletesTheme() {
        // given
        Long themeId = 1L;
        when(reservationRepository.existsByThemeId(themeId)).thenReturn(false);
        when(themeRepository.existsById(themeId)).thenReturn(true);
        doNothing().when(themeRepository).deleteById(themeId);

        // when
        themeService.deleteThemeById(themeId);

        // then
        verify(themeRepository, times(1)).deleteById(themeId);
    }

    @Test
    @DisplayName("이미 예약이 있는 테마를 삭제하면 예외가 발생한다")
    void deleteThemeById_WithExistingReservation_ThrowsIllegalStateException() {
        // given
        Long themeId = 1L;
        when(reservationRepository.existsByThemeId(themeId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(themeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이 테마는 이미 예약이 존재합니다");
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 예외가 발생한다")
    void deleteThemeById_WithNonExistingId_ThrowsNotFoundException() {
        // given
        Long themeId = 999L;
        when(reservationRepository.existsByThemeId(themeId)).thenReturn(false);
        when(themeRepository.existsById(themeId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> themeService.deleteThemeById(themeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("등록된 테마만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("인기 있는 테마를 조회할 수 있다")
    void findPopularThemes_ReturnsPopularThemes() {
        // given
        Theme theme1 = testTheme;
        Theme theme2 = new Theme(2L, "Popular Theme", "Popular Description", "popular-thumbnail.jpg");
        List<Theme> popularThemes = Arrays.asList(theme1, theme2);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);
        
        when(themeRepository.findPopular(start, end)).thenReturn(popularThemes);

        // when
        List<ThemeResponse> responses = themeService.findPopularThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("Test Theme");
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).name()).isEqualTo("Popular Theme");
    }
}
