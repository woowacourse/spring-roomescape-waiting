package roomescape.feature.reservation.cancel;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@ExtendWith(MockitoExtension.class)
class WaitingPromoterTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingPromoter waitingPromoter;

    private Time time() {
        return Time.reconstruct(TIME_ID, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private Theme theme() {
        return Theme.reconstruct(THEME_ID, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 가장_빠른_대기_예약을_확정한다 {

        @Test
        void 가장_빠른_순번의_대기_예약을_WAITING에서_ACTIVE로_확정한다() {
            // given
            Reservation waiting = Reservation.reconstruct(
                    1L, new ReserverName("예약자"), DATE, time(), theme(), ReservationStatus.WAITING);
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(Optional.of(waiting));
            when(reservationRepository.changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE))
                    .thenReturn(1);

            // when
            waitingPromoter.promoteFastestWaiting(new Slot(TIME_ID, THEME_ID, DATE));

            // then
            verify(reservationRepository).changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
        }

        @Test
        void 슬롯에_이미_ACTIVE_예약이_있으면_승격하지_않는다() {
            // given
            when(reservationRepository.existsActiveReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(true);

            // when
            waitingPromoter.promoteFastestWaiting(new Slot(TIME_ID, THEME_ID, DATE));

            // then
            verify(reservationRepository, never()).findLowestIdWaitingReservation(any(), any(), any());
            verify(reservationRepository, never()).changeStatus(any(), any(), any());
        }

        @Test
        void 대기_예약이_없으면_아무것도_확정하지_않는다() {
            // given
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(Optional.empty());

            // when
            waitingPromoter.promoteFastestWaiting(new Slot(TIME_ID, THEME_ID, DATE));

            // then
            verify(reservationRepository, never()).changeStatus(any(), any(), any());
        }

        @Test
        void 후보_대기가_그_사이_취소되면_다음_순번을_승격한다() {
            // given
            Reservation first = Reservation.reconstruct(
                    1L, new ReserverName("1순위"), DATE, time(), theme(), ReservationStatus.WAITING);
            Reservation second = Reservation.reconstruct(
                    2L, new ReserverName("2순위"), DATE, time(), theme(), ReservationStatus.WAITING);
            when(reservationRepository.findLowestIdWaitingReservation(DATE, TIME_ID, THEME_ID))
                    .thenReturn(Optional.of(first))
                    .thenReturn(Optional.of(second));
            when(reservationRepository.changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE))
                    .thenReturn(0);
            when(reservationRepository.changeStatus(2L, ReservationStatus.WAITING, ReservationStatus.ACTIVE))
                    .thenReturn(1);

            // when
            waitingPromoter.promoteFastestWaiting(new Slot(TIME_ID, THEME_ID, DATE));

            // then
            verify(reservationRepository).changeStatus(1L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
            verify(reservationRepository).changeStatus(2L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
        }
    }
}
