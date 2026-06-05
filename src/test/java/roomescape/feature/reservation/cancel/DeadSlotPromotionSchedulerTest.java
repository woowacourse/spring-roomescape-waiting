package roomescape.feature.reservation.cancel;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.reservation.domain.SlotKey;
import roomescape.feature.reservation.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class DeadSlotPromotionSchedulerTest {

    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private WaitingPromoter waitingPromoter;

    @InjectMocks
    private DeadSlotPromotionScheduler deadSlotPromotionScheduler;

    private static SlotKey slotKey(long timeId, long themeId) {
        return new SlotKey(DATE, timeId, themeId);
    }

    @Test
    void 죽은_슬롯이_없으면_승격을_시도하지_않는다() {
        // given
        when(reservationRepository.findDeadSlotKeys()).thenReturn(List.of());

        // when
        deadSlotPromotionScheduler.promoteDeadSlots();

        // then
        verifyNoInteractions(waitingPromoter);
    }

    @Test
    void 죽은_슬롯마다_가장_빠른_대기를_승격한다() {
        // given
        SlotKey firstDeadSlotKey = slotKey(1L, 1L);
        SlotKey secondDeadSlotKey = slotKey(2L, 2L);
        when(reservationRepository.findDeadSlotKeys()).thenReturn(List.of(firstDeadSlotKey, secondDeadSlotKey));

        // when
        deadSlotPromotionScheduler.promoteDeadSlots();

        // then
        verify(waitingPromoter).promoteFastestWaiting(firstDeadSlotKey);
        verify(waitingPromoter).promoteFastestWaiting(secondDeadSlotKey);
    }

    @Test
    void 한_슬롯의_승격이_실패해도_나머지_슬롯을_계속_승격한다() {
        // given
        SlotKey failingDeadSlotKey = slotKey(1L, 1L);
        SlotKey remainingDeadSlotKey = slotKey(2L, 2L);
        when(reservationRepository.findDeadSlotKeys()).thenReturn(List.of(failingDeadSlotKey, remainingDeadSlotKey));

        doThrow(new RuntimeException("승격 실패"))
                .when(waitingPromoter).promoteFastestWaiting(failingDeadSlotKey);

        // when & then
        assertThatNoException().isThrownBy(() -> deadSlotPromotionScheduler.promoteDeadSlots());
        verify(waitingPromoter).promoteFastestWaiting(remainingDeadSlotKey);
    }
}
