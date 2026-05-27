package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;

class WaitingReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-05-05T05:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private ReservationRepository reservationRepository;
    private WaitingReservationRepository waitingReservationRepository;
    private ReservationDateService reservationDateService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private WaitingReservationService waitingReservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        waitingReservationRepository = mock(WaitingReservationRepository.class);
        reservationDateService = mock(ReservationDateService.class);
        reservationTimeService = mock(ReservationTimeService.class);
        themeService = mock(ThemeService.class);
        waitingReservationService = new WaitingReservationService(
            waitingReservationRepository,
            reservationRepository,
            reservationDateService,
            reservationTimeService,
            themeService,
            FIXED_CLOCK
        );
    }

    @Test
    void 이미_다른_사용자에_의해_예약된_슬롯에_대기를_신청할_수_있다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 10));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);
        WaitingReservation savedWaiting = WaitingReservation.of(
            10L,
            "고래",
            date,
            time,
            theme,
            LocalDateTime.of(2026, 5, 5, 14, 0)
        );

        when(reservationDateService.findById(1L)).thenReturn(date);
        when(reservationTimeService.findById(2L)).thenReturn(time);
        when(themeService.findById(3L)).thenReturn(theme);
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(true);
        when(waitingReservationRepository.save(any(WaitingReservation.class))).thenReturn(savedWaiting);

        WaitingReservationCreationResponse response = waitingReservationService.createWaitingReservation(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("고래");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 5, 14, 0));

        verify(waitingReservationRepository, times(1)).save(any(WaitingReservation.class));
    }
}
