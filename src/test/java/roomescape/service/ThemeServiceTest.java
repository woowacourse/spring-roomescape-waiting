package roomescape.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeJpaRepository;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeJpaRepository themeJpaRepository;
    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 존재하지_않는_테마_조회_시_예외가_발생한다() {
        // given
        given(themeJpaRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> themeService.find(999L)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_테마_삭제_시_예외가_발생한다() {
        // given
        given(themeJpaRepository.existsById(999L)).willReturn(false);

        // when & then
        Assertions.assertThatThrownBy(() -> themeService.delete(999L)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 유명한_테마_조회가_정상적으로_호출되어야_한다() {
        // given
        ThemeFamousFindRequest request = RoomEscapeFixture.themeFamousFindRequest();

        // when
        themeService.findFamous(request, LocalDate.now());

        // then
        verify(themeRepository).findFamous(any());
    }

    @Test
    void 삭제시_테마를_사용하는_예외가_있으면_예외가_발생한다() {
        given(themeJpaRepository.existsById(1L)).willReturn(true);
        given(reservationRepository.existsByThemeId(1L)).willReturn(true);
        Assertions.assertThatThrownBy(() -> themeService.delete(1L)).isInstanceOf(RoomEscapeException.class);
    }
}
