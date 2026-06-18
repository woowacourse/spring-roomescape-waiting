package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.dto.RankProjection;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.RoomescapeException;

class WaitingReservationServiceTest {

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
            themeService
        );
    }

    @Test
    void 이미_다른_사용자에_의해_예약된_슬롯에_대기를_신청할_수_있다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.now().plusDays(5));
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

    @Test
    void 비어있는_슬롯에_대기를_신청하면_예외가_발생한다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.now().plusDays(5));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateService.findById(1L)).thenReturn(date);
        when(reservationTimeService.findById(2L)).thenReturn(time);
        when(themeService.findById(3L)).thenReturn(theme);
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(false);

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("예약 가능한 시간에는 대기를 신청할 수 없습니다.");
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복_대기할_수_없다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.now().plusDays(5));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateService.findById(1L)).thenReturn(date);
        when(reservationTimeService.findById(2L)).thenReturn(time);
        when(themeService.findById(3L)).thenReturn(theme);
        when(reservationRepository.existsByDateIdAndTimeIdAndThemeId(1L, 2L, 3L)).thenReturn(true);
        when(waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId("고래", 1L, 2L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("중복으로 대기 신청을 할 수 없습니다.");
    }

    @Test
    void 과거_시간에는_예약_대기를_신청할_수_없다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 5));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(13, 59));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservationCreationRequest request = new WaitingReservationCreationRequest("고래", 1L, 2L, 3L);

        when(reservationDateService.findById(1L)).thenReturn(date);
        when(reservationTimeService.findById(2L)).thenReturn(time);
        when(themeService.findById(3L)).thenReturn(theme);

        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("과거 예약은 수정 및 취소가 불가능합니다.");
    }

    @Test
    void 이름으로_예약_대기와_순번을_조회할_수_있다() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.of(2026, 5, 10));
        ReservationTime time = ReservationTime.of(2L, LocalTime.of(10, 0));
        Theme theme = Theme.of(3L, "공포", "테마 내용", "/themes/scary");
        WaitingReservation waiting = WaitingReservation.of(
            10L, "이산", date, time, theme, LocalDateTime.of(2026, 5, 5, 14, 0)
        );
        RankProjection rankProjection = new RankProjection() {
            public Long getId() { return 10L; }
            public Long getRank() { return 5L; }
        };

        when(waitingReservationRepository.findAllByName("이산")).thenReturn(List.of(waiting));
        when(waitingReservationRepository.findRankByName("이산")).thenReturn(List.of(rankProjection));

        List<WaitingReservationWithRankResponse> result = waitingReservationService
            .getWaitingReservationsWithRankByName("이산");
        WaitingReservationWithRankResponse response = result.get(0);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("이산");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(response.time().startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.theme().id()).isEqualTo(3L);
        assertThat(response.rank()).isEqualTo(5L);
    }
}
