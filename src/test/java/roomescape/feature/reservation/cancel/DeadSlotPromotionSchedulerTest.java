package roomescape.feature.reservation.cancel;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;

@ExtendWith(MockitoExtension.class)
class DeadSlotPromotionSchedulerTest {

    private static final LocalDate DATE = LocalDate.now().plusYears(1);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private WaitingPromoter waitingPromoter;

    @InjectMocks
    private DeadSlotPromotionScheduler deadSlotPromotionScheduler;

    private static Slot slot(long timeId, long themeId) {
        return new Slot(
                DATE,
                Time.reconstruct(timeId, LocalTime.of(10, 0), EntityStatus.ACTIVE),
                Theme.reconstruct(themeId, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE));
    }

    @Test
    void 죽은_슬롯이_없으면_승격을_시도하지_않는다() {
        // given
        when(reservationRepository.findDeadSlots()).thenReturn(List.of());

        // when
        deadSlotPromotionScheduler.promoteDeadSlots();

        // then
        verifyNoInteractions(waitingPromoter);
    }

    @Test
    void 죽은_슬롯마다_가장_빠른_대기를_승격한다() {
        // given
        Slot firstDeadSlot = slot(1L, 1L);
        Slot secondDeadSlot = slot(2L, 2L);
        when(reservationRepository.findDeadSlots()).thenReturn(List.of(firstDeadSlot, secondDeadSlot));

        // when
        deadSlotPromotionScheduler.promoteDeadSlots();

        // then
        verify(waitingPromoter).promoteFastestWaiting(firstDeadSlot);
        verify(waitingPromoter).promoteFastestWaiting(secondDeadSlot);
    }

    @Test
    void 한_슬롯의_승격이_실패해도_나머지_슬롯을_계속_승격한다() {
        // given
        Slot failingDeadSlot = slot(1L, 1L);
        Slot remainingDeadSlot = slot(2L, 2L);
        when(reservationRepository.findDeadSlots()).thenReturn(List.of(failingDeadSlot, remainingDeadSlot));

        doThrow(new RuntimeException("승격 실패"))
                .when(waitingPromoter).promoteFastestWaiting(failingDeadSlot);

        // when & then
        assertThatNoException().isThrownBy(() -> deadSlotPromotionScheduler.promoteDeadSlots());
        verify(waitingPromoter).promoteFastestWaiting(remainingDeadSlot);
    }
}
