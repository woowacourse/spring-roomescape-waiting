package roomescape.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.waiting.Waiting;
import roomescape.reservation.waiting.WaitingRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    private ReservationWaitingService reservationWaitingService;
    private ReservationRepository reservationRepository;
    private WaitingRepository waitingRepository;

    private Member member;
    private Schedule schedule;
    private List<Waiting> waitings;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        waitingRepository = mock(WaitingRepository.class);
        reservationWaitingService = new ReservationWaitingService(reservationRepository, waitingRepository);

        ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
        Theme theme = themeWithId(1L, new Theme("테마명", "테마 설명", "썸네일 URL"));
        schedule = scheduleWithId(1L, new Schedule(LocalDate.now().plusDays(1), reservationTime, theme));
        member = memberWithId(1L, new Member("email@example.com", "password", "사용자", MemberRole.MEMBER));
    }

    @Test
    @DisplayName("예약 삭제 시, 나머지 웨이팅의 대기 번호를 감소시킨다")
    void changeFirstWaitingToReservation() {
        // given
        Waiting firstWaiting = waitingWithId(1L, new Waiting(schedule, member, 1L));
        Waiting secondWaiting = waitingWithId(2L, new Waiting(schedule, member, 2L));
        Waiting thirdWaiting = waitingWithId(3L, new Waiting(schedule, member, 3L));
        waitings = List.of(firstWaiting, secondWaiting, thirdWaiting);

        Reservation reservation = reservationWithId(1L, new Reservation(member, schedule));
        given(reservationRepository.findById(1L)).willReturn(java.util.Optional.of(reservation));
        given(waitingRepository.findAllBySchedule(schedule)).willReturn(waitings);
        given(reservationRepository.save(any(Reservation.class))).willReturn(new Reservation(firstWaiting.getMember(), firstWaiting.getSchedule()));

        // when
        reservationWaitingService.deleteReservationById(1L);

        // then
        assertThat(secondWaiting.getRank()).isEqualTo(1L);
        assertThat(thirdWaiting.getRank()).isEqualTo(2L);
    }
}
