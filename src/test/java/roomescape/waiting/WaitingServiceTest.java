package roomescape.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    private WaitingService waitingService;
    private WaitingRepository waitingRepository;
    private ReservationService reservationService;
    private ScheduleService scheduleService;
    private MemberService memberService;

    private WaitingRequest REQUEST;
    private LoginMember LOGIN_MEMBER;
    private Schedule SCHEDULE;
    private Member MEMBER;

    @BeforeEach
    void setUp() {
        REQUEST = new WaitingRequest(LocalDate.now().plusDays(1), 1L, 1L);
        LOGIN_MEMBER = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
        ReservationTime reservationTime = reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40)));
        Theme theme = themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123"));
        SCHEDULE = scheduleWithId(1L, new Schedule(REQUEST.date(), reservationTime, theme));
        MEMBER = memberWithId(1L, new Member(LOGIN_MEMBER.email(), "password", "boogie", MemberRole.MEMBER));

        waitingRepository = mock(WaitingRepository.class);
        reservationService = mock(ReservationService.class);
        scheduleService = mock(ScheduleService.class);
        memberService = mock(MemberService.class);
        waitingService = new WaitingService(waitingRepository, reservationService, scheduleService, memberService);
    }

    @Test
    @DisplayName("웨이팅을 할 수 있다.")
    void createWaiting() {
        // given
        given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), REQUEST.timeId(), REQUEST.themeId()))
                .willReturn(SCHEDULE);
        given(reservationService.existsBySchedule(SCHEDULE))
                .willReturn(true);
        given(memberService.findByEmail(LOGIN_MEMBER.email()))
                .willReturn(MEMBER);

        Waiting waiting1 = new Waiting(SCHEDULE, MEMBER, 1L);
        Waiting waiting2 = new Waiting(SCHEDULE, MEMBER, 2L);
        Waiting waiting3 = new Waiting(SCHEDULE, MEMBER, 3L);
        List<Waiting> waitings = List.of(waiting1, waiting2, waiting3);
        given(waitingRepository.findAllBySchedule(SCHEDULE))
                .willReturn(waitings);

        Waiting waiting = new Waiting(SCHEDULE, MEMBER, (long) waitings.size() + 1);
        Waiting createdWaiting = waitingWithId(1L, waiting);
        given(waitingRepository.save(any()))
                .willReturn(createdWaiting);

        // when
        WaitingResponse waitingResponse = waitingService.create(REQUEST, LOGIN_MEMBER);

        // then
        assertThat(WaitingResponse.of(createdWaiting))
                .isEqualTo(waitingResponse);
    }

    @Test
    @DisplayName("스케줄에 대한 예약이 없는 경우, 웨이팅을 할 수 없다")
    void createWaiting2() {
        // given
        given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), REQUEST.timeId(), REQUEST.themeId()))
                .willReturn(SCHEDULE);
        given(reservationService.existsBySchedule(SCHEDULE))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingService.create(REQUEST, LOGIN_MEMBER))
                .isInstanceOf(ReservationNotExistsScheduleException.class);
    }

    @Test
    @DisplayName("권한이 없는 경우 웨이팅을 취소할 수 없다")
    void deleteWaiting2() {
        // given
        LOGIN_MEMBER = new LoginMember("may", "may@email.com", MemberRole.MEMBER);
        Long waitingId = 1L;
        Waiting waiting = waitingWithId(waitingId, new Waiting(SCHEDULE, MEMBER, 1L));

        given(waitingRepository.findById(waitingId))
                .willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> waitingService.deleteById(waitingId, LOGIN_MEMBER))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("고객은 본인의 예약 대기를 삭제할 수 있다")
    void deleteWaiting3() {
        // given
        LOGIN_MEMBER = new LoginMember("may", LOGIN_MEMBER.email(), MemberRole.MEMBER);
        Long waitingId = 1L;
        Waiting waiting = waitingWithId(waitingId, new Waiting(SCHEDULE, MEMBER, 1L));

        given(waitingRepository.findById(waitingId))
                .willReturn(Optional.of(waiting));

        // when & then
        assertDoesNotThrow(() -> waitingService.deleteById(waitingId, LOGIN_MEMBER));
    }
}
