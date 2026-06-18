package roomescape.service.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;
import static roomescape.common.config.FixedClockConfig.NOW_TIME;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
import roomescape.infrastructure.SlotManager;
import roomescape.service.processor.WaitingPromotionProcessor;

@ExtendWith(MockitoExtension.class)
class EmptyReservationDetectionSchedulerTest {
    private final UserName userName = new UserName("브라운");

    private final LocalDateTime today = LocalDateTime.of(LocalDate.parse(TODAY), LocalTime.parse(NOW_TIME));
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));

    private final Theme theme = new Theme(1L, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));

    private final EventSlot slot = new EventSlot(futureDate, time, theme);
    private final Waiting waiting = new Waiting(1L, userName, slot, today);

    @Mock
    private WaitingDao waitingDao;
    @Mock
    private SlotManager slotManager;
    @Mock
    private WaitingPromotionProcessor promotionProcessor;

    private EmptyReservationDetectionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new EmptyReservationDetectionScheduler(waitingDao, slotManager, promotionProcessor);
    }

    @Test
    @DisplayName("스케줄러가 대기가 있는 빈 예약을 조회하고, 대기-예약 전환을 호출한다.")
    void processWaiting_Promotions_CallsProcessor() {
        given(waitingDao.findUnreservedWaiting()).willReturn(List.of(waiting));
        given(slotManager.tryAcquire(slot)).willReturn(true);

        scheduler.processWaitingPromotion();

        then(promotionProcessor).should(times(1)).promoteWaiting(slot);
    }
}
