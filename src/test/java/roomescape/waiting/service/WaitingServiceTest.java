package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.common.Constant.MATT;
import static roomescape.common.Constant.예약날짜_내일;

import java.time.LocalTime;
import java.util.Optional;
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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
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
    @Mock
    private ReservationRepository reservationRepository;

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
        assertThat(response.name()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(savedReservationTime.getId(),
                        savedReservationTime.getStartAt().toString()));
    }

    @Test
    void 대기를_승인한다() {
        //given
        Member savedMember = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        Waiting savedWaiting = new Waiting(1L, 예약날짜_내일, savedReservationTime, savedTheme,
                savedMember);
        Reservation reservation = new Reservation(1L, 예약날짜_내일.getDate(), savedReservationTime, savedTheme, savedMember);
        when(waitingRepository.findById(any(Long.class))).thenReturn(Optional.of(savedWaiting));
        when(reservationRepository.existsByReservationDateAndReservationTimeId(any(ReservationDate.class),
                any(Long.class)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        //when
        ReservationResponse response = waitingService.approveWaitingById(1L);

        //then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(savedReservationTime.getId(),
                        savedReservationTime.getStartAt().toString()));
    }

    @Test
    void 이미_예약이_존재한다면_대기를_거절한다() {
        //given
        Member savedMember = new Member(1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), Role.MEMBER);
        ReservationTime savedReservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme savedTheme = new Theme(1L, "test", "test", "test");
        Waiting savedWaiting = new Waiting(1L, 예약날짜_내일, savedReservationTime, savedTheme,
                savedMember);
        when(waitingRepository.findById(any(Long.class))).thenReturn(Optional.of(savedWaiting));
        when(reservationRepository.existsByReservationDateAndReservationTimeId(any(ReservationDate.class),
                any(Long.class)))
                .thenReturn(true);

        //when-then
        assertThatThrownBy(() -> waitingService.approveWaitingById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 현재 예약이 존재합니다. 취소 후 다시 요청해 주세요.");
    }

    @Test
    void 존재하지_않는_대기는_승인할_수_없다() {
        //given
        when(waitingRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        //when-then
        assertThatThrownBy(() -> waitingService.approveWaitingById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 존재하지 않는 대기입니다.");
    }

}
