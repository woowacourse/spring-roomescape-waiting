package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.RoomEscapeException;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 존재하지_않는_테마_조회_시_예외가_발생한다() {
        // given
        given(themeRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> themeService.find(999L)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_테마_삭제_시_예외가_발생한다() {
        // given
        given(themeRepository.existsById(999L)).willReturn(false);

        // when & then
        Assertions.assertThatThrownBy(() -> themeService.delete(999L)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 유명한_테마_조회_시_모든_매개변수가_존재하면_그대로_전달된다() {
        // given
        long days = 10L;
        LocalDate date = LocalDate.parse("2026-05-01");
        long limit = 20L;

        // when
        themeService.findFamous(new ThemeFamousFindRequest(days, date, limit), LocalDate.now());

        // then
        verify(themeRepository).findFamous(days, date, limit);
    }

    @Test
    void 유명한_테마_조회_시_days가_없으면_기본_값으로_대체된다() {
        // given
        LocalDate date = LocalDate.parse("2026-05-01");
        long limit = 20L;

        // when
        themeService.findFamous(new ThemeFamousFindRequest(null, date, limit), LocalDate.now());

        // then
        verify(themeRepository).findFamous(7L, date, limit);
    }

    @Test
    void 유명한_테마_조회_시_date가_없으면_오늘로_대체된다() {
        // given
        long days = 7L;
        long limit = 10L;
        LocalDate now = LocalDate.now();
        // when
        themeService.findFamous(new ThemeFamousFindRequest(days, null, limit), now);

        // then
        verify(themeRepository).findFamous(days, now, limit);
    }

    @Test
    void 유명한_테마_조회_시_limit이_없으면_기본값으로_대체된다() {
        // given
        long days = 7L;
        LocalDate date = LocalDate.parse("2026-05-01");

        // when
        themeService.findFamous(new ThemeFamousFindRequest(days, date, null), LocalDate.now());

        // then
        verify(themeRepository).findFamous(days, date, 10L);
    }

    @Test
    void 삭제시_테마가_존재하지_않으면_예외가_발생한다() {
        given(themeRepository.existsById(999L)).willReturn(false);
        Assertions.assertThatThrownBy(() -> themeService.delete(999L)).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 삭제시_테마를_사용하는_슬롯이_있으면_예외가_발생한다() {
        given(themeRepository.existsById(1L)).willReturn(true);
        given(slotRepository.existsByThemeId(1L)).willReturn(true);
        Assertions.assertThatThrownBy(() -> themeService.delete(1L)).isInstanceOf(RoomEscapeException.class);
    }
}
