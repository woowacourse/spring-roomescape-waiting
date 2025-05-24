package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.common.Constant.MATT;
import static roomescape.common.Constant.예약날짜_내일;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.request.ReservationRequest;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private ReservationTimeService reservationTimeService;
    @Mock
    private ThemeService themeService;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    void 대기를_생성한다() {
        Member savedMember = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        Waiting savedWaiting = new Waiting(1L, 예약날짜_내일, savedReservationTime, savedTheme,
                savedMember);
        when(memberService.findById(any(Long.class))).thenReturn(savedMember);
        when(reservationTimeService.getReservationTime(any(Long.class))).thenReturn(savedReservationTime);
        when(themeService.getTheme(any(Long.class))).thenReturn(savedTheme);
        when(waitingRepository.save(any())).thenReturn(savedWaiting);

        ReservationRequest reservationRequest = new ReservationRequest(
                예약날짜_내일.getDate(),
                savedReservationTime.getId(),
                savedTheme.getId()
        );

        ReservationResponse response = waitingService.createById(1L, reservationRequest);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.member().getName()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(savedReservationTime.getId(),
                        savedReservationTime.getStartAt().toString()));
    }

}
