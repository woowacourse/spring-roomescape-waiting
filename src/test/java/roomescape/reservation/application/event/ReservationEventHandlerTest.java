package roomescape.reservation.application.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.application.PromoteService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReservationEventHandlerTest {

    @InjectMocks
    private ReservationEventHandler eventHandler;

    @Mock
    private PromoteService promoteService;

    @DisplayName("예약 삭제 이벤트 처리 - 예약이 있는 경우 승격 서비스 호출")
    @Test
    void handleDeleteEvent_withReservation() {
        // given
        Member member = MemberFixture.createMember("회원", "member@example.com", "password123");
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("테마", "설명", "썸네일");

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        Reservation reservation = new Reservation(member, spec);
        ReservationDeletedEvent event = new ReservationDeletedEvent(Optional.of(reservation));

        // when
        eventHandler.handleDeleteEvent(event);

        // then
        verify(promoteService).promoteWaiting(reservation);
    }

    @DisplayName("예약 삭제 이벤트 처리 - 예약이 없는 경우 아무 작업도 수행하지 않음")
    @Test
    void handleDeleteEvent_withoutReservation() {
        // given
        ReservationDeletedEvent event = new ReservationDeletedEvent(Optional.empty());

        // when
        eventHandler.handleDeleteEvent(event);

        // then
        verify(promoteService, never()).promoteWaiting(any());
    }
}
