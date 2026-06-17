package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.dto.request.ThemeCreateRequest;
import roomescape.dto.response.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마_생성() {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest("우주 탈출", "우주선에서 탈출하는 재미있는 테마입니다.", "https://example.com/space.jpg");
        Theme savedTheme = Theme.createWithId(1L, "우주 탈출", "우주선에서 탈출하는 재미있는 테마입니다.", "https://example.com/space.jpg");

        given(themeRepository.save(any(Theme.class))).willReturn(savedTheme);

        // when
        ThemeResult response = themeService.create(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("우주 탈출");
        assertThat(response.description()).isEqualTo("우주선에서 탈출하는 재미있는 테마입니다.");
        assertThat(response.thumbnailUrl()).isEqualTo("https://example.com/space.jpg");
    }

    @Test
    void 테마_삭제() {
        // given
        Long targetThemeId = 1L;
        given(themeRepository.deleteById(targetThemeId)).willReturn(true);

        // when
        themeService.delete(targetThemeId);

        // then
        verify(themeRepository).deleteById(targetThemeId);
    }

    @Test
    void 삭제하려는_테마에_이미_예약이_존재할_경우_예외발생() {
        // given
        Long targetThemeId = 1L;
        given(reservationRepository.existsByThemeId(targetThemeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.delete(targetThemeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_HAS_RESERVATION);

        verify(themeRepository, org.mockito.Mockito.never()).deleteById(anyLong());
    }

    @Test
    void 삭제하려는_테마가_DB에_존재하지_않을시_예외밯생() {
        // given
        Long targetThemeId = 1L;
        given(themeRepository.deleteById(targetThemeId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> themeService.delete(targetThemeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
    }

    @Test
    void 인기_테마_목록_조회() {
        // given
        Theme theme1 = Theme.createWithId(1L, "호러 병원", "아주 무서운 병원 테마입니다.", "https://example.com/hospital.jpg");
        Theme theme2 = Theme.createWithId(2L, "마법 학교", "마법사가 되어 방을 탈출하세요.", "https://example.com/magic.jpg");

        given(themeRepository.findPopularThemes(any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of(theme1, theme2));

        // when
        List<ThemeResult> responses = themeService.getPopularThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("호러 병원");
        assertThat(responses.get(1).id()).isEqualTo(2L);

        verify(themeRepository).findPopularThemes(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void 전체_테마_목록_조회() {
        // given
        Theme theme = Theme.createWithId(1L, "감옥 탈출", "감옥에서 조용히 빠져나가는 테마", "https://example.com/prison.jpg");
        given(themeRepository.findAll()).willReturn(List.of(theme));

        // when
        List<ThemeResult> responses = themeService.getThemes();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
        assertThat(responses.getFirst().name()).isEqualTo("감옥 탈출");

        verify(themeRepository).findAll();
    }
}
