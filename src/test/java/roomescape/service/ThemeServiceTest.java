package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.theme.ThemeRepository;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

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
        given(themeRepository.getById(999L)).willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));

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
    void 유명한_테마_조회_시_모든_매개변수_그대로_전달된다() {
        // given
        int days = 10;
        LocalDate date = LocalDate.parse("2026-05-01");
        int limit = 20;

        // when
        themeService.findFamous(limit, days, date);

        // then
        verify(themeRepository).findFamous(date.minusDays(days), date, PageRequest.of(0, limit));
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
