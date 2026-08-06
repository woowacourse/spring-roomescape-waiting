package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlotResolver;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.support.exception.RoomescapeException;

class WaitingReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-05-05T05:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private ReservationRepository reservationRepository;
    private WaitingReservationRepository waitingReservationRepository;
    private ReservationDateRepository reservationDateRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private WaitingReservationService waitingReservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        waitingReservationRepository = mock(WaitingReservationRepository.class);
        reservationDateRepository = mock(ReservationDateRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        waitingReservationService = new WaitingReservationService(
            waitingReservationRepository,
            reservationRepository,
            new ReservationSlotResolver(reservationDateRepository, reservationTimeRepository, themeRepository),
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

        when(reservationDateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(true);
        when(waitingReservationRepository.save(any(WaitingReservation.class))).thenReturn(savedWaiting);

        WaitingReservationCreationResponse response = waitingReservationService.createWaitingReservation(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("고래");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 5, 14, 0));

        verify(waitingReservationRepository, times(1)).save(any(WaitingReservation.class));
    }

    @Test
    void 비어있는_슬롯에_대기를_신청하면_예외가_발생한다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 10));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(false);

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("예약 가능한 시간에는 대기를 신청할 수 없습니다.");
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복_대기할_수_없다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 10));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(true);
        when(waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId("고래", 1L, 2L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("중복으로 대기 신청을 할 수 없습니다.");
    }

    @Test
    void 마감된_일시에는_예약_대기를_신청할_수_없다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 5));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(14, 9));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateRepository.findById(1L)).thenReturn(Optional.of(date));
        when(reservationTimeRepository.findById(2L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(3L)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("예약 시작 10분 전부터는 예약 대기를 신청할 수 없습니다.");
    }

    @Test
    void 존재하지_않는_예약_대기를_취소하면_예외가_발생한다() {
        when(waitingReservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> waitingReservationService.cancelWaitingReservation(999L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("해당하는 예약 대기를 찾을 수 없습니다.");
    }
}
