package roomescape.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.domain.Theme;
import roomescape.domain.WaitingOrder;
import roomescape.repository.LockedReservationWriter;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeLockedAction;
import roomescape.service.dto.ReservationCreateCommand;
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
    private static final LocalDate VALID_RESERVATION_DATE = LocalDate.of(2026, 5, 9);
    private static final ReservationCreateCommand VALID_COMMAND_MOA = new ReservationCreateCommand(
            "모아", VALID_RESERVATION_DATE, 1L, 1L
    );

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private LockedReservationWriter reservationWriter;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @InjectMocks
    private AdminReservationService reservationService;

    private void stubThemeLock(Optional<Theme> lockedTheme) {
        given(reservationRepository.executeWithThemeLock(eq(1L), any()))
                .willAnswer(invocation -> {
                    ThemeLockedAction<Object> action = invocation.getArgument(1);
                    return action.execute(lockedTheme, reservationWriter);
                });
    }

    @Test
    @DisplayName("같은 이름+날짜+시간+테마에 이미 예약이 있으면 ReservationConflictException이 발생한다")
    void 같은_날짜_시간_테마에_이미_예약이_있으면_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        stubThemeLock(Optional.of(VALID_THEME));
        given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L))
                .willReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
        verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L);
    }

    @Test
    @DisplayName("충돌이 없으면 정상적으로 예약을 생성한다")
    void 충돌이_없으면_정상적으로_예약을_생성한다() {
        ReservationWithWaitingOrder saved = new ReservationWithWaitingOrder(
                1L, VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED, new WaitingOrder(0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        stubThemeLock(Optional.of(VALID_THEME));
        given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L))
                .willReturn(false);
        given(reservationWriter.save(any(Reservation.class))).willReturn(saved);

        assertDoesNotThrow(() -> reservationService.create(VALID_COMMAND_MOA));

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
        verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeId(
                VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L);
        verify(reservationWriter, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약을 생성하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약을 생성하면 ThemeNotFoundException이 발생한다")
    void 존재하지_않는_themeId로_예약시_예외가_발생한다() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        stubThemeLock(Optional.empty());

        assertThrows(
                ThemeNotFoundException.class,
                () -> reservationService.create(VALID_COMMAND_MOA)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
    }

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 ReservationNotFoundException이 발생한다")
    void 존재하지_않는_예약_취소시_예외가_발생한다() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationNotFoundException.class,
                () -> reservationService.cancel(1L)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationTimeRepository);
    }

    @Test
    @DisplayName("확정 예약을 취소하면 soft delete 후 첫 대기자를 승급한다")
    void 확정_예약_취소시_취소하고_승급한다() {
        Reservation confirmed = new Reservation(
                1L, "브라운", VALID_RESERVATION_DATE, VALID_TIME, VALID_THEME, ReservationStatus.CONFIRMED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(confirmed));
        stubThemeLock(Optional.of(VALID_THEME));

        assertDoesNotThrow(() -> reservationService.cancel(1L));

        verify(reservationRepository, times(2)).findById(1L);
        verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
        verify(reservationWriter, times(1)).cancel(1L);
        verify(reservationWriter, times(1)).promoteEarliestWaiting(VALID_RESERVATION_DATE, 1L, 1L);
        verifyNoInteractions(reservationTimeRepository);
    }

    @Nested
    class 테마_날짜_시간대가_같을_때 {

        @Test
        @DisplayName("해당 타임 슬롯에 예약이 없다면 얘약을 허용한다")
        void 해당_타임_슬롯에_예약이_없다면_예약을_허용한다() {

            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
            stubThemeLock(Optional.of(VALID_THEME));
            given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId("모아", VALID_COMMAND_MOA.date(), 1L,
                    1L))
                    .willReturn(false);
            given(reservationWriter.save(any(Reservation.class))).willReturn(new ReservationWithWaitingOrder(
                    1L, "모아", VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME,
                    ReservationStatus.CONFIRMED, new WaitingOrder(0)));

            assertDoesNotThrow(() -> reservationService.create(VALID_COMMAND_MOA));

            verify(reservationTimeRepository, times(1)).findById(1L);
            verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
            verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeId("모아",
                    VALID_COMMAND_MOA.date(), 1L,
                    1L);
            verify(reservationWriter, times(1)).save(any(Reservation.class));
        }

        @Nested
        class 해당_타임_슬롯에_이미_예약이_있을_때 {

            @Test
            @DisplayName("기존 예약자와 동일한 사용자의 예약 요청이라면 거부한다")
            void 사용자_이름이_같으면_ReservationConflictException을_던진다() {
                given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
                stubThemeLock(Optional.of(VALID_THEME));
                given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                        VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L))
                        .willReturn(true);

                assertThrows(
                        ReservationConflictException.class,
                        () -> reservationService.create(VALID_COMMAND_MOA)
                );

                verify(reservationTimeRepository, times(1)).findById(1L);
                verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
                verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeId(
                        VALID_COMMAND_MOA.reserverName(), VALID_COMMAND_MOA.date(), 1L, 1L);

            }

            @Test
            @DisplayName("기존 예약자와 다른 사용자의 예약 요청이라면 허용한다")
            void 사용자_이름이_다르면_예약_대기_순번을_부여한다() {
                given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
                stubThemeLock(Optional.of(VALID_THEME));
                given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId("모아", VALID_COMMAND_MOA.date(), 1L,
                        1L))
                        .willReturn(false);
                given(reservationRepository.existsActiveConfirmed(VALID_COMMAND_MOA.date(), 1L, 1L))
                        .willReturn(true);
                given(reservationWriter.save(any(Reservation.class))).willReturn(new ReservationWithWaitingOrder(
                        1L, "모아", VALID_COMMAND_MOA.date(), VALID_TIME, VALID_THEME,
                        ReservationStatus.WAITING, new WaitingOrder(1)));

                assertDoesNotThrow(() -> reservationService.create(VALID_COMMAND_MOA));

                verify(reservationTimeRepository, times(1)).findById(1L);
                verify(reservationRepository, times(1)).executeWithThemeLock(eq(1L), any());
                verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeId("모아",
                        VALID_COMMAND_MOA.date(), 1L,
                        1L);
                verify(reservationWriter, times(1)).save(any(Reservation.class));
            }
        }
    }
}
