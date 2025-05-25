package roomescape.booking.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.waiting.dto.WaitingRequest;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
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
