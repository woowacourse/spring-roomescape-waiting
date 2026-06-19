package roomescape.service.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;
import static roomescape.common.config.FixedClockConfig.NOW_TIME;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
import roomescape.infrastructure.SlotManager;

@ExtendWith(MockitoExtension.class)
class WaitingPromotionProcessorTest {
    private final UserName userName = new UserName("브라운");

    private final LocalDateTime today = LocalDateTime.of(LocalDate.parse(TODAY), LocalTime.parse(NOW_TIME));
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);

    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));

    private final Theme theme = new Theme(1L, ThemeName.parse("테마1"), Description.parse("설명1"),
            ThumbnailUrl.parse("/images/thumbnail"));

    private final EventSlot slot = new EventSlot(futureDate, time, theme);
    private final Waiting waiting = new Waiting(1L, userName, slot, today);

    private WaitingPromotionProcessor promotionProcessor;

    @Mock
    private WaitingDao waitingDao;
    @Mock
    private ReservationDao reservationDao;
    @Mock
    private SlotManager slotManager;

    @BeforeEach
    void setUp() {
        promotionProcessor = new WaitingPromotionProcessor(waitingDao, reservationDao, slotManager);
    }

    @Test
    @DisplayName("예약 대기를 승격한다.")
    void promoteWaiting_Success() {
        given(waitingDao.findByEventSlot(slot)).willReturn(Optional.of(waiting));

        promotionProcessor.promoteWaiting(slot);

        then(reservationDao).should(times(1)).save(any(Reservation.class));
        then(waitingDao).should(times(1)).delete(waiting.getId());
        then(slotManager).should(never()).release(any());
    }

    @Test
    @DisplayName("예약 대기가 없으면 슬롯을 비운다.")
    void promoteWaiting_WhenNothingWaiting_Success() {
        given(waitingDao.findByEventSlot(slot)).willReturn(Optional.empty());

        promotionProcessor.promoteWaiting(slot);

        then(slotManager).should(times(1)).release(slot);
        then(reservationDao).should(never()).save(any(Reservation.class));
        then(waitingDao).should(never()).delete(waiting.getId());
    }
}
