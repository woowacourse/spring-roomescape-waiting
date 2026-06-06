package roomescape.application.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.domain.theme.ThemeRepository;
import roomescape.presentation.theme.request.CreateThemeRequest;
import roomescape.presentation.theme.response.AdminThemesResponse;
import roomescape.presentation.theme.response.CreateThemeResponse;
import roomescape.presentation.theme.response.PopularThemesResponse;
import roomescape.presentation.theme.response.ThemesResponse;

@DisplayName("테마 서비스")
@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2030-01-08T00:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationSlotRepository slotRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, slotRepository, FIXED_CLOCK);
    }

    @DisplayName("테마 목록을 조회할 수 있다")
    @Test
    void getAllTheme() {
        // given
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(themeRepository.findAll()).willReturn(List.of(theme));

        // when
        ThemesResponse response = themeService.getAllTheme();

        // then
        assertThat(response.themes()).hasSize(1);
        assertThat(response.themes()).singleElement()
                .satisfies((Object payload) -> assertThat(payload)
                        .extracting("name")
                        .isEqualTo("심해 공포"));
        verify(themeRepository, times(1)).findAll();
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("관리자용 테마 목록을 조회할 수 있다")
    @Test
    void getAllThemeForAdmin() {
        // given
        Theme theme = Theme.of(1L, "도심 추격전", "도심에서 벌어지는 추격 테마", "/themes/chase");
        given(themeRepository.findAll()).willReturn(List.of(theme));

        // when
        AdminThemesResponse response = themeService.getAllThemeForAdmin();

        // then
        assertThat(response.themes()).hasSize(1);
        assertThat(response.themes()).singleElement()
                .satisfies((Object payload) -> assertThat(payload)
                        .extracting("url")
                        .isEqualTo("/themes/chase"));
        verify(themeRepository, times(1)).findAll();
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("최근 인기 테마를 조회할 수 있다")
    @Test
    void getThemeRank() {
        // given
        Theme popularTheme = Theme.of(1L, "도심 추격전", "도심에서 벌어지는 추격 테마", "/themes/chase");
        Theme lessPopularTheme = Theme.of(2L, "고대 유적 탐험", "고대 유적을 탐험하는 테마", "/themes/ruins");
        given(themeRepository.findPopularThemes(10, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 8)))
                .willReturn(List.of(
                        ThemeRankResult.of(popularTheme, 1),
                        ThemeRankResult.of(lessPopularTheme, 2)
                ));

        // when
        PopularThemesResponse response = themeService.getThemeRank();

        // then
        assertThat(response.popularThemes()).hasSize(2);
        assertThat(response.popularThemes()).element(0)
                .satisfies((Object payload) -> {
                    assertThat(payload).extracting("name").isEqualTo("도심 추격전");
                    assertThat(payload).extracting("rank").isEqualTo(1);
                });
        assertThat(response.popularThemes()).element(1)
                .satisfies((Object payload) -> {
                    assertThat(payload).extracting("name").isEqualTo("고대 유적 탐험");
                    assertThat(payload).extracting("rank").isEqualTo(2);
                });
        verify(themeRepository, times(1)).findPopularThemes(10, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 8));
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("테마를 저장할 수 있다")
    @Test
    void createTheme() {
        // given
        CreateThemeRequest request = new CreateThemeRequest("심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        Theme savedTheme = Theme.of(10L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(themeRepository.save(any(Theme.class))).willReturn(savedTheme);

        // when
        CreateThemeResponse response = themeService.createTheme(request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("심해 공포");
        assertThat(response.content()).isEqualTo("심해 탈출 공포 테마");
        assertThat(response.url()).isEqualTo("/themes/deep-sea");
        verify(themeRepository, times(1)).save(any(Theme.class));
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("사용 중이 아닌 테마는 삭제할 수 있다")
    @Test
    void deleteTheme() {
        // given
        given(slotRepository.existsByThemeId(1L)).willReturn(false);
        given(themeRepository.deleteById(1L)).willReturn(1);

        // when
        themeService.deleteTheme(1L);

        // then
        verify(slotRepository, times(1)).existsByThemeId(1L);
        verify(themeRepository, times(1)).deleteById(1L);
    }

    @DisplayName("사용 중인 테마는 삭제할 수 없다")
    @Test
    void deleteThemeWhenInUse() {
        // given
        given(slotRepository.existsByThemeId(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.THEME_IN_USE);
        verify(slotRepository, times(1)).existsByThemeId(1L);
        verify(themeRepository, never()).deleteById(anyLong());
    }

    @DisplayName("존재하지 않는 테마는 삭제할 수 없다")
    @Test
    void deleteThemeWhenNotFound() {
        // given
        given(slotRepository.existsByThemeId(1L)).willReturn(false);
        given(themeRepository.deleteById(1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.THEME_NOT_FOUND);
        verify(slotRepository, times(1)).existsByThemeId(1L);
        verify(themeRepository, times(1)).deleteById(1L);
    }
}
