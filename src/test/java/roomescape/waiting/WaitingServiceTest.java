package roomescape.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.ReservationService;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleService;
import roomescape.booking.waiting.Waiting;
import roomescape.booking.waiting.WaitingRepository;
import roomescape.booking.waiting.WaitingService;
import roomescape.booking.waiting.dto.WaitingRequest;
import roomescape.booking.waiting.dto.WaitingResponse;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsScheduleException;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.member.MemberService;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationService reservationService;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private MemberService memberService;
    @InjectMocks
    private WaitingService waitingService;

    private WaitingRequest request;
    private LoginMember loginMember;
    private Schedule schedule;
    private Member member;

    @BeforeEach
    void setUp() {
        request = new WaitingRequest(LocalDate.now().plusDays(1), 1L, 1L);
        loginMember = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
        ReservationTime reservationTime = reservationTimeWithId(request.timeId(), new ReservationTime(LocalTime.of(12, 40)));
        Theme theme = themeWithId(request.themeId(), new Theme("야당", "야당당", "123"));
        schedule = scheduleWithId(1L, new Schedule(request.date(), reservationTime, theme));
        member = memberWithId(1L, new Member(loginMember.email(), "password", "boogie", MemberRole.MEMBER));
    }

    @Test
    @DisplayName("웨이팅을 할 수 있다.")
    void createWaiting() {
        // given
        given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationService.existsBySchedule(schedule))
                .willReturn(true);
        given(memberService.findByEmail(loginMember.email()))
                .willReturn(member);

        Waiting waiting = new Waiting(schedule, member, LocalDateTime.now().minusDays(1));
        Waiting createdWaiting = waitingWithId(1L, waiting);
        given(waitingRepository.save(any()))
                .willReturn(createdWaiting);

        // when
        WaitingResponse waitingResponse = waitingService.create(request, loginMember);

        // then
        assertThat(WaitingResponse.of(createdWaiting))
                .isEqualTo(waitingResponse);
    }

    @Test
    @DisplayName("스케줄에 대한 예약이 없는 경우, 웨이팅을 할 수 없다")
    void createWaiting2() {
        // given
        given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationService.existsBySchedule(schedule))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingService.create(request, loginMember))
                .isInstanceOf(ReservationNotExistsScheduleException.class);
    }

    @Test
    @DisplayName("권한이 없는 경우 웨이팅을 취소할 수 없다")
    void deleteWaiting2() {
        // given
        loginMember = new LoginMember("may", "may@email.com", MemberRole.MEMBER);
        Long waitingId = 1L;
        Waiting waiting = waitingWithId(waitingId, new Waiting(schedule, member, LocalDateTime.now()));

        given(waitingRepository.findById(waitingId))
                .willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> waitingService.deleteById(waitingId, loginMember))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("고객은 본인의 예약 대기를 삭제할 수 있다")
    void deleteWaiting3() {
        // given
        loginMember = new LoginMember("may", loginMember.email(), MemberRole.MEMBER);
        Long waitingId = 1L;
        Waiting waiting = waitingWithId(waitingId, new Waiting(schedule, member, LocalDateTime.now()));

        given(waitingRepository.findById(waitingId))
                .willReturn(Optional.of(waiting));

        // when & then
        assertDoesNotThrow(() -> waitingService.deleteById(waitingId, loginMember));
    }
}
