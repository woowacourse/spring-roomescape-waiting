package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ThemeCreateCommand;
import roomescape.service.dto.ThemeResult;
import roomescape.service.exception.ThemeConflictException;
import roomescape.service.exception.ThemeInUseException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme VALID_THEME = new Theme(1L, "테마", "설명", "url");

    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ThemeService themeService;

    @Test
    @DisplayName("같은 이름의 테마가 이미 등록되어 있으면 ThemeConflictException이 발생한다")
    void 같은_이름의_테마가_이미_있으면_예외가_발생한다() {
        ThemeCreateCommand command = new ThemeCreateCommand("테마", "설명", "url");
        given(themeRepository.existsByName("테마")).willReturn(true);

        assertThrows(
                ThemeConflictException.class,
                () -> themeService.create(command)
        );
    }

    @Test
    @DisplayName("같은 이름의 테마가 없으면 정상적으로 테마를 생성한다")
    void 같은_이름의_테마가_없으면_정상_생성한다() {
        ThemeCreateCommand command = new ThemeCreateCommand("테마", "설명", "url");
        given(themeRepository.existsByName("테마")).willReturn(false);
        given(themeRepository.save(any(Theme.class)))
                .willReturn(new Theme(1L, "테마", "설명", "url"));

        ThemeResult created = themeService.create(command);

        assertThat(created).isEqualTo(new ThemeResult(1L, "테마", "설명", "url", 0L));
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 ThemeNotFoundException이 발생한다")
    void 존재하지_않는_테마_삭제시_예외가_발생한다() {
        given(themeRepository.existsById(1L)).willReturn(false);

        assertThrows(
                ThemeNotFoundException.class,
                () -> themeService.delete(1L)
        );
    }

    @Test
    @DisplayName("예약이 존재하는 테마를 삭제하면 ThemeInUseException이 발생한다")
    void 예약이_존재하는_테마_삭제시_예외가_발생한다() {
        given(themeRepository.existsById(1L)).willReturn(true);
        given(reservationRepository.existsByThemeId(1L)).willReturn(true);

        assertThrows(
                ThemeInUseException.class,
                () -> themeService.delete(1L)
        );
    }

    @Test
    @DisplayName("예약이 없는 테마는 정상적으로 삭제된다")
    void 예약이_없는_테마는_정상_삭제된다() {
        given(themeRepository.existsById(1L)).willReturn(true);
        given(reservationRepository.existsByThemeId(1L)).willReturn(false);

        assertDoesNotThrow(() -> themeService.delete(1L));
    }
}
