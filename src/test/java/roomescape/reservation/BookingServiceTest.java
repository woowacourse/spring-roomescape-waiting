package roomescape.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.booking.BookingService;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationService;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.waiting.Waiting;
import roomescape.booking.waiting.WaitingService;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private WaitingService waitingService;
    @InjectMocks
    private BookingService bookingService;

    private Member member;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
        Theme theme = themeWithId(1L, new Theme("테마명", "테마 설명", "썸네일 URL"));
        schedule = scheduleWithId(1L, new Schedule(LocalDate.now().plusDays(1), reservationTime, theme));
        member = memberWithId(1L, new Member("email@example.com", "password", "사용자", MemberRole.MEMBER));
    }

    @Test
    @DisplayName("예약 삭제 시, 그 스케줄의 첫 번째 웨이팅을 예약으로 변경한다")
    void changeFirstWaitingToReservation() {
        // given
        Waiting firstWaiting = waitingWithId(1L, new Waiting(schedule, member, 1L));
        Reservation reservation = reservationWithId(1L, new Reservation(member, schedule));
        given(reservationService.findById(1L)).willReturn(reservation);
        given(waitingService.existsBySchedule(schedule)).willReturn(true);
        given(waitingService.findFirstWaitingOfSchedule(schedule)).willReturn(firstWaiting);

        // when
        bookingService.deleteReservationById(1L);

        // then
        then(reservationService).should().save(new Reservation(firstWaiting.getMember(), firstWaiting.getSchedule()));
    }

    @Test
    @DisplayName("예약 삭제 시, 그 스케줄의 웨이팅들 대기 순번이 감소한다.")
    void changeRanksOfWaitings() {
        // given
        Waiting firstWaiting = waitingWithId(1L, new Waiting(schedule, member, 1L));

        Reservation reservation = reservationWithId(1L, new Reservation(member, schedule));
        given(reservationService.findById(1L)).willReturn(reservation);
        given(waitingService.existsBySchedule(schedule)).willReturn(true);
        given(waitingService.findFirstWaitingOfSchedule(schedule)).willReturn(firstWaiting);

        // when
        bookingService.deleteReservationById(1L);

        // then
        then(waitingService).should().decreaseRankOfSchedule(schedule);
    }
}
