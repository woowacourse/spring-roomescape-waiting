package roomescape.unit.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeMockTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 5);
    private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);
    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final Long RESERVATION_ID = 99L;
    private static final ReservationTime TIME = new ReservationTime(TIME_ID, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(THEME_ID, "공포", "무서운 테마", "https://example.com/horror.jpg");

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationWaitingService reservationWaitingService;

    @InjectMocks
    private ReservationFacade facade;

    @Test
    void 본인이_예약한_슬롯에는_대기를_신청할_수_없다() {
        Reservation owned = new Reservation(RESERVATION_ID, "민욱", DATE, TIME, THEME);
        given(reservationService.getById(RESERVATION_ID)).willReturn(owned);

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", RESERVATION_ID);

        assertThatThrownBy(() -> facade.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("본인이 예약한");
    }

    @Test
    void 존재하지_않는_예약에는_대기를_신청할_수_없다() {
        given(reservationService.getById(RESERVATION_ID))
                .willThrow(new NotFoundException("ID 99번 예약을 찾을 수 없습니다."));

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", RESERVATION_ID);

        assertThatThrownBy(() -> facade.addWaiting(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 지난_예약에는_대기를_신청할_수_없다() {
        Reservation past = new Reservation(RESERVATION_ID, "티뉴", PAST_DATE, TIME, THEME);
        given(reservationService.getById(RESERVATION_ID)).willReturn(past);

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", RESERVATION_ID);

        assertThatThrownBy(() -> facade.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 대기");
        verify(reservationWaitingService, never()).addWaiting(any(ReservationWaiting.class));
    }

    @Test
    void 다른_사람이_예약한_슬롯에는_대기_신청이_저장소까지_도달한다() {
        Reservation other = new Reservation(RESERVATION_ID, "티뉴", DATE, TIME, THEME);
        given(reservationService.getById(RESERVATION_ID)).willReturn(other);

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", RESERVATION_ID);

        facade.addWaiting(request);

        verify(reservationWaitingService).addWaiting(any(ReservationWaiting.class));
    }

    @Test
    void 이미_대기를_신청한_예약에는_다시_신청할_수_없다() {
        Reservation other = new Reservation(RESERVATION_ID, "티뉴", DATE, TIME, THEME);
        given(reservationService.getById(RESERVATION_ID)).willReturn(other);
        given(reservationWaitingService.existBy("민욱", RESERVATION_ID)).willReturn(true);

        ReservationWaitingRequest request = new ReservationWaitingRequest("민욱", RESERVATION_ID);

        assertThatThrownBy(() -> facade.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 대기");
    }

    @Test
    void 이미_지난_예약은_변경할_수_없다() {
        Long reservationId = 1L;
        String name = "민욱";
        Reservation past = new Reservation(reservationId, name, PAST_DATE, TIME, THEME);
        given(reservationService.findMyReservation(reservationId, name)).willReturn(past);

        ReservationUpdateRequest request = new ReservationUpdateRequest(DATE, TIME_ID);

        assertThatThrownBy(() -> facade.updateMyReservation(reservationId, name, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 지난 예약");
    }
}
