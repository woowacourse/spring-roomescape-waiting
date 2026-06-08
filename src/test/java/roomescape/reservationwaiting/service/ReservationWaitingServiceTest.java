package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDate.now().atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationWaitingFactory reservationWaitingFactory;
    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    private ReservationTime time;
    private Theme theme;
    private Reservation futureReservation;

    @BeforeEach
    void setUp() {
        time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.restore(1L, "테마A", "설명", "https://a.com");
        futureReservation = Reservation.restore(1L, "user1",
                new ReservationSlot(LocalDate.of(2099, 12, 1), time, theme));
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    void 예약_대기_생성_실패() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(futureReservation));
        when(reservationWaitingRepository.isWaitingBy(futureReservation.getSlot(), "현미밥")).thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.createWaiting(new ReservationWaitingRequest("현미밥", 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_WAITING))
                .hasMessage(ErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("지난 예약 대기는 삭제할 수 없다.")
    void 예약_대기_삭제_실패() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, "현미밥", LocalDate.now().minusDays(1), time, theme);
        when(reservationWaitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_WAITING_CANCEL))
                .hasMessage(ErrorCode.PAST_WAITING_CANCEL.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 대기 ID로 삭제 시 예외 발생")
    void 없는_대기_삭제_실패() {
        when(reservationWaitingRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.WAITING_NOT_FOUND))
                .hasMessage(ErrorCode.WAITING_NOT_FOUND.getMessage());
    }
}
