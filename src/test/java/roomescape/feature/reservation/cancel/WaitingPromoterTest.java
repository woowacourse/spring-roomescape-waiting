package roomescape.feature.reservation.cancel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.dao.OptimisticLockingFailureException;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.reservation.domain.SlotKey;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@ExtendWith(MockitoExtension.class)
class WaitingPromoterTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final LocalDate DATE = LocalDate.now().plusYears(1);
    private static final Slot SLOT = new Slot(DATE, time(), theme());
    private static final SlotKey SLOT_KEY = SLOT.toSlotKey();

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingPromoter waitingPromoter;

    private static Time time() {
        return Time.reconstruct(TIME_ID, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private static Theme theme() {
        return Theme.reconstruct(THEME_ID, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 가장_빠른_대기_예약을_확정한다 {

        @Test
        void 가장_빠른_순번의_대기_예약을_WAITING에서_ACTIVE로_확정한다() {
            // given
            Reservation waiting = Reservation.reconstruct(
                    1L, new ReserverName("예약자"), DATE, time(), theme(), ReservationStatus.WAITING);
            when(reservationRepository.findLowestIdWaitingReservation(SLOT_KEY))
                    .thenReturn(Optional.of(waiting));

            // when
            waitingPromoter.promoteFastestWaiting(SLOT_KEY);

            // then
            verify(reservationRepository).changeStatus(1L, 0L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
        }

        @Test
        void 슬롯에_이미_ACTIVE_예약이_있으면_승격하지_않는다() {
            // given
            when(reservationRepository.existsActiveReservation(SLOT_KEY))
                    .thenReturn(true);

            // when
            waitingPromoter.promoteFastestWaiting(SLOT_KEY);

            // then
            verify(reservationRepository, never()).findLowestIdWaitingReservation(any());
            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }

        @Test
        void 대기_예약이_없으면_아무것도_확정하지_않는다() {
            // given
            when(reservationRepository.findLowestIdWaitingReservation(SLOT_KEY))
                    .thenReturn(Optional.empty());

            // when
            waitingPromoter.promoteFastestWaiting(SLOT_KEY);

            // then
            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }

        @Test
        void 후보_대기가_그_사이_변경되어_낙관적_락_예외가_발생하면_다음_순번을_승격한다() {
            // given
            Reservation first = Reservation.reconstruct(
                    1L, new ReserverName("1순위"), DATE, time(), theme(), ReservationStatus.WAITING);
            Reservation second = Reservation.reconstruct(
                    2L, new ReserverName("2순위"), DATE, time(), theme(), ReservationStatus.WAITING);
            when(reservationRepository.findLowestIdWaitingReservation(SLOT_KEY))
                    .thenReturn(Optional.of(first))
                    .thenReturn(Optional.of(second));
            doThrow(new OptimisticLockingFailureException("후보 대기가 그 사이 변경됨"))
                    .when(reservationRepository)
                    .changeStatus(1L, 0L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);

            // when
            waitingPromoter.promoteFastestWaiting(SLOT_KEY);

            // then
            verify(reservationRepository).changeStatus(1L, 0L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
            verify(reservationRepository).changeStatus(2L, 0L, ReservationStatus.WAITING, ReservationStatus.ACTIVE);
        }
    }
}
