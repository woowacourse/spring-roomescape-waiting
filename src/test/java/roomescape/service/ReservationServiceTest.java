package roomescape.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme VALID_THEME = new Theme(
            1L,
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );
    private static final ReservationCreateCommand VALID_COMMAND = new ReservationCreateCommand(
            "브라운", LocalDate.of(2026, 5, 9), 1L, 1L
    );

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @InjectMocks
    private AdminReservationService reservationService;

    @Test
    @DisplayName("같은 날짜+시간+테마에 이미 예약이 있으면 ReservationConflictException이 발생한다")
    void 같은_날짜_시간_테마에_이미_예약이_있으면_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findById(1L)).willReturn(Optional.of(VALID_THEME));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(VALID_COMMAND.date(), 1L, 1L))
                .willReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> reservationService.create(VALID_COMMAND)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).existsByDateAndTimeIdAndThemeId(VALID_COMMAND.date(), 1L, 1L);
    }

    @Test
    @DisplayName("충돌이 없으면 정상적으로 예약을 생성한다")
    void 충돌이_없으면_정상적으로_예약을_생성한다() {
        ReservationWithWaitingOrder saved = new ReservationWithWaitingOrder(
                1L, "브라운", VALID_COMMAND.date(), VALID_TIME, VALID_THEME, 0L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findById(1L)).willReturn(Optional.of(VALID_THEME));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(VALID_COMMAND.date(), 1L, 1L))
                .willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(saved);

        assertDoesNotThrow(() -> reservationService.create(VALID_COMMAND));

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).existsByDateAndTimeIdAndThemeId(VALID_COMMAND.date(), 1L, 1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약을 생성하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(themeRepository, reservationRepository);
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약을 생성하면 ThemeNotFoundException이 발생한다")
    void 존재하지_않는_themeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ThemeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 ReservationNotFoundException이 발생한다")
    void 존재하지_않는_예약_삭제시_예외가_발생한다() {
        given(reservationRepository.existsById(1L)).willReturn(false);

        assertThrows(
                ReservationNotFoundException.class,
                () -> reservationService.delete(1L)
        );

        verify(reservationRepository, times(1)).existsById(1L);
        verifyNoInteractions(reservationTimeRepository, themeRepository);
    }

    @Test
    @DisplayName("존재하는 예약은 정상적으로 삭제된다")
    void 존재하는_예약은_정상_삭제된다() {
        given(reservationRepository.existsById(1L)).willReturn(true);

        assertDoesNotThrow(() -> reservationService.delete(1L));

        verify(reservationRepository, times(1)).existsById(1L);
        verify(reservationRepository, times(1)).deleteById(1L);
        verifyNoInteractions(reservationTimeRepository, themeRepository);
    }
}
