package roomescape.feature.reservation.cancel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@ExtendWith(MockitoExtension.class)
class ActiveReservationCancelHandlerTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ActiveReservationCancelHandler reservationCancelHandler;

    private Time time() {
        return Time.reconstruct(TIME_ID, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private Theme theme() {
        return Theme.reconstruct(THEME_ID, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 가장_빠른_대기_예약을_확정한다 {

        @Test
        void 가장_빠른_순번의_대기_예약을_ACTIVE로_확정한다() {
            // given
            Reservation waiting = Reservation.reconstruct(
                    1L, new ReserverName("예약자"), DATE, time(), theme(), ReservationStatus.WAITING);
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(Optional.of(waiting));

            // when
            reservationCancelHandler.confirmFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE));

            // then
            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        }

        @Test
        void 대기_예약이_없으면_아무것도_확정하지_않는다() {
            // given
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(Optional.empty());

            // when
            reservationCancelHandler.confirmFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE));

            // then
            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 승격_처리_중_예외가_발생해도_예외를_전파하지_않는다() {
            // given
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenThrow(new RuntimeException("DB 조회 실패"));

            // when & then
            assertThatNoException().isThrownBy(() ->
                    reservationCancelHandler.confirmFastestWaiting(new ActiveReservationCancelEvent(TIME_ID, THEME_ID, DATE)));
        }
    }
}
