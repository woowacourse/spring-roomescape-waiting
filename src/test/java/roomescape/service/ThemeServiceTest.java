package roomescape.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeLockedAction;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ThemeCreateCommand;
import roomescape.service.exception.ThemeConflictException;
import roomescape.service.exception.ThemeInUseException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private static final ThemeCreateCommand VALID_COMMAND = new ThemeCreateCommand(
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );

    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ThemeService themeService;

    private void stubThemeLock() {
        given(reservationRepository.executeWithThemeLock(eq(1L), any()))
                .willAnswer(invocation -> {
                    ThemeLockedAction<Object> action = invocation.getArgument(1);
                    return action.execute(Optional.empty(), null);
                });
    }

    @Test
    @DisplayName("같은 이름의 테마가 이미 있으면 ThemeConflictException이 발생한다")
    void 같은_이름_테마가_있으면_예외가_발생한다() {
        given(themeRepository.existsByName(VALID_COMMAND.name())).willReturn(true);

        assertThrows(
                ThemeConflictException.class,
                () -> themeService.create(VALID_COMMAND)
        );

        verify(themeRepository, times(1)).existsByName(VALID_COMMAND.name());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("같은 이름의 테마가 없으면 정상적으로 테마를 생성한다")
    void 같은_이름_테마가_없으면_정상_생성한다() {
        Theme saved = new Theme(
                1L, VALID_COMMAND.name(), VALID_COMMAND.description(), VALID_COMMAND.thumbnailUrl());
        given(themeRepository.existsByName(VALID_COMMAND.name())).willReturn(false);
        given(themeRepository.save(any(Theme.class))).willReturn(saved);

        assertDoesNotThrow(() -> themeService.create(VALID_COMMAND));

        verify(themeRepository, times(1)).existsByName(VALID_COMMAND.name());
        verify(themeRepository, times(1)).save(any(Theme.class));
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 ThemeNotFoundException이 발생한다")
    void 존재하지_않는_테마_삭제시_예외가_발생한다() {
        given(themeRepository.existsById(1L)).willReturn(false);

        assertThrows(
                ThemeNotFoundException.class,
                () -> themeService.delete(1L)
        );

        verify(themeRepository, times(1)).existsById(1L);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("예약이 존재하는 테마를 삭제하면 ThemeInUseException이 발생한다")
    void 예약이_존재하는_테마_삭제시_예외가_발생한다() {
        given(themeRepository.existsById(1L)).willReturn(true);
        stubThemeLock();
        given(reservationRepository.existsByThemeId(1L)).willReturn(true);

        assertThrows(
                ThemeInUseException.class,
                () -> themeService.delete(1L)
        );

        verify(themeRepository, times(1)).existsById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
        verify(reservationRepository, times(1)).existsByThemeId(1L);
    }

    @Test
    @DisplayName("예약이 없는 테마는 정상적으로 삭제된다")
    void 예약이_없는_테마는_정상_삭제된다() {
        given(themeRepository.existsById(1L)).willReturn(true);
        stubThemeLock();
        given(reservationRepository.existsByThemeId(1L)).willReturn(false);

        assertDoesNotThrow(() -> themeService.delete(1L));

        verify(themeRepository, times(1)).existsById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
        verify(reservationRepository, times(1)).existsByThemeId(1L);
        verify(themeRepository, times(1)).deleteById(1L);
    }
}
