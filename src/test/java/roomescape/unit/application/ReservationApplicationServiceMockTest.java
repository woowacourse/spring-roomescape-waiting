package roomescape.unit.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.ReservationApplicationService;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationWaitingFixture;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationApplicationServiceMockTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 5);
    private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);
    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 1L;
    private static final ReservationTime TIME = new ReservationTime(TIME_ID, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(
            THEME_ID,
            "공포",
            "무서운 테마",
            "https://example.com/horror.jpg"
    );

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationApplicationService applicationService;

    @Test
    void 본인이_예약한_슬롯에는_대기를_신청할_수_없다() {
        Reservation owned = ReservationFixture.builder().id(99L).name("민욱").build();
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(DATE, THEME_ID))
                .willReturn(new Reservations(List.of(owned)));

        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                DATE,
                TIME_ID,
                THEME_ID
        );

        assertThatThrownBy(() -> applicationService.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("본인이 예약한");
    }

    @Test
    void 예약되지_않은_슬롯에는_대기를_신청할_수_없다() {
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(DATE, THEME_ID))
                .willReturn(new Reservations(List.of()));

        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                DATE,
                TIME_ID,
                THEME_ID
        );

        assertThatThrownBy(() -> applicationService.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("예약된 슬롯");
    }

    @Test
    void 지난_예약에는_대기를_신청할_수_없다() {
        Reservation past = ReservationFixture.builder().id(99L).name("티뉴").date(PAST_DATE).build();
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(PAST_DATE, THEME_ID))
                .willReturn(new Reservations(List.of(past)));

        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                PAST_DATE,
                TIME_ID,
                THEME_ID
        );

        assertThatThrownBy(() -> applicationService.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 대기");
        verify(reservationWaitingService, never()).addWaiting(any(ReservationWaiting.class));
    }

    @Test
    void 다른_사람이_예약한_슬롯에는_대기_신청이_저장소까지_도달한다() {
        Reservation other = ReservationFixture.builder().id(99L).name("티뉴").build();
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(DATE, THEME_ID))
                .willReturn(new Reservations(List.of(other)));

        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                DATE,
                TIME_ID,
                THEME_ID
        );

        applicationService.addWaiting(request);

        verify(reservationWaitingService).addWaiting(any(ReservationWaiting.class));
    }

    @Test
    void 이미_대기를_신청한_예약에는_다시_신청할_수_없다() {
        Reservation other = ReservationFixture.builder().id(99L).name("티뉴").build();
        given(reservationTimeService.findById(TIME_ID)).willReturn(TIME);
        given(themeService.findById(THEME_ID)).willReturn(THEME);
        given(reservationService.findByDateAndThemeId(DATE, THEME_ID))
                .willReturn(new Reservations(List.of(other)));
        given(reservationWaitingService.existBy("민욱", 99L)).willReturn(true);

        ReservationWaitingRequest request = new ReservationWaitingRequest(
                "민욱",
                DATE,
                TIME_ID,
                THEME_ID
        );

        assertThatThrownBy(() -> applicationService.addWaiting(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 대기");
    }

    @Test
    void 이미_지난_예약은_변경할_수_없다() {
        Long reservationId = 1L;
        String name = "민욱";
        Reservation past = ReservationFixture.builder().id(reservationId).name(name).date(PAST_DATE).build();
        given(reservationService.findMyReservation(reservationId, name)).willReturn(past);

        ReservationUpdateRequest request = new ReservationUpdateRequest(DATE, TIME_ID);

        assertThatThrownBy(() -> applicationService.updateMyReservation(reservationId, name, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("이미 지난 예약");
    }

    @Test
    void cancelMyReservation은_지난_예약이면_BusinessRuleViolationException을_던진다() {
        Reservation past = ReservationFixture.builder().id(1L).name("민욱").date(PAST_DATE).build();
        given(reservationService.findMyReservation(1L, "민욱")).willReturn(past);

        assertThatThrownBy(() -> applicationService.cancelMyReservation(1L, "민욱"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(reservationService, never()).deleteReservation(anyLong());
        verify(reservationService, never()).changeOwner(anyLong(), anyString());
    }

    @Test
    void cancelMyReservation은_대기가_없으면_예약을_삭제한다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("민욱").build();
        given(reservationService.findMyReservation(1L, "민욱")).willReturn(reservation);
        given(reservationWaitingService.findEarliestByReservationId(1L)).willReturn(Optional.empty());

        applicationService.cancelMyReservation(1L, "민욱");

        verify(reservationService).deleteReservation(1L);
        verify(reservationService, never()).changeOwner(anyLong(), anyString());
    }

    @Test
    void cancelMyReservation은_대기가_있으면_대기_1번을_예약으로_전환한다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("민욱").build();
        ReservationWaiting first = ReservationWaitingFixture.builder().id(10L).name("브라운").build();
        given(reservationService.findMyReservation(1L, "민욱")).willReturn(reservation);
        given(reservationWaitingService.findEarliestByReservationId(1L)).willReturn(Optional.of(first));

        applicationService.cancelMyReservation(1L, "민욱");

        verify(reservationService).changeOwner(1L, "브라운");
        verify(reservationWaitingService).deleteById(10L);
        verify(reservationService, never()).deleteReservation(anyLong());
    }

    @Test
    void deleteReservation은_미래_슬롯에_대기가_있으면_대기_1번을_예약으로_전환한다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("민욱").build();
        ReservationWaiting first = ReservationWaitingFixture.builder().id(10L).name("브라운").build();
        given(reservationService.findReservation(1L)).willReturn(Optional.of(reservation));
        given(reservationWaitingService.findEarliestByReservationId(1L)).willReturn(Optional.of(first));

        applicationService.deleteReservation(1L);

        verify(reservationService).changeOwner(1L, "브라운");
        verify(reservationWaitingService).deleteById(10L);
    }

    @Test
    void deleteReservation은_대기가_없으면_예약을_삭제한다() {
        Reservation reservation = ReservationFixture.builder().id(1L).name("민욱").build();
        given(reservationService.findReservation(1L)).willReturn(Optional.of(reservation));
        given(reservationWaitingService.findEarliestByReservationId(1L)).willReturn(Optional.empty());

        applicationService.deleteReservation(1L);

        verify(reservationService).deleteReservation(1L);
    }

    @Test
    void deleteReservation은_지난_예약이면_BusinessRuleViolationException을_던진다() {
        Reservation past = ReservationFixture.builder().id(1L).name("티뉴").date(PAST_DATE).build();
        given(reservationService.findReservation(1L)).willReturn(Optional.of(past));

        assertThatThrownBy(() -> applicationService.deleteReservation(1L))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(reservationService, never()).deleteReservation(anyLong());
        verify(reservationService, never()).changeOwner(anyLong(), anyString());
    }
}
