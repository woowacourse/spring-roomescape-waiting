package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.service.event.ReservationCancelledEvent;

@ExtendWith(MockitoExtension.class)
public class WaitingPromotionTest {
    @Mock
    private WaitingService waitingService;

    @Mock
    private ReservationRepository reservationRepository;

    private WaitingPromotionListener listener;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
        listener = new WaitingPromotionListener(waitingService, reservationRepository, fixedClock);
    }

    @Test
    void 이벤트_수신_시_대기_1번이_예약으로_전환된다() {
        // given
        Waiting waiting = new Waiting(
                1L,
                "밍구",
                LocalDate.of(2026, 5, 10),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")
        );

        ReservationCancelledEvent event = new ReservationCancelledEvent(1L, LocalDate.of(2026, 5, 10), 1L, 1L);

        when(waitingService.findFirstWaiting(event.getDate(), event.getTimeId(), event.getThemeId()))
                .thenReturn(Optional.of(waiting));

        // when
        listener.onReservationCancelled(event);

        // then
        verify(waitingService).deleteForPromotion(waiting);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(waiting.getName());
        assertThat(saved.getDate()).isEqualTo(waiting.getDate());
        assertThat(saved.getTime()).isEqualTo(waiting.getTime());
        assertThat(saved.getTheme()).isEqualTo(waiting.getTheme());
    }

    @Test
    void 이벤트_수신_시_대기가_없으면_승격하지_않는다() {
        // given
        ReservationCancelledEvent event = new ReservationCancelledEvent(1L, LocalDate.of(2026, 5, 10), 1L, 1L);

        when(waitingService.findFirstWaiting(event.getDate(), event.getTimeId(), event.getThemeId()))
                .thenReturn(Optional.empty());

        // when
        listener.onReservationCancelled(event);

        // then
        verify(waitingService, never()).deleteForPromotion(any(Waiting.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
