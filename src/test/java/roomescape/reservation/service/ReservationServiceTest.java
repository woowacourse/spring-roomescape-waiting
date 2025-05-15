package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.InvalidTimeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.dto.admin.AdminReservationRequest;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.theme.domain.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private ReservationTimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;
    private MemberRepository memberRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        timeRepository = mock(ReservationTimeRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        themeRepository = mock(ThemeRepository.class);
        memberRepository = mock(MemberRepository.class);

        reservationService = new ReservationService(reservationRepository, timeRepository, themeRepository,
                memberRepository);
    }

    @DisplayName("예약 추가 시 현재보다 과거 시간인 경우 예외를 발생시킨다")
    @Test
    void exception_time_before() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        UserReservationRequest userReservationRequest = new UserReservationRequest(LocalDate.now(), 1L, 1L);
        assertThatThrownBy(() -> reservationService.add(1L, userReservationRequest))
                .isInstanceOf(InvalidTimeException.class);

        verify(timeRepository, times(1)).findById(1L);
    }

    @DisplayName("예약 추가 시 동일 일자와 시간에 예약이 존재하는 경우 예외를 발생시킨다")
    @Test
    void exception_not_available() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(reservationRepository.existsByDateAndTimeId(LocalDate.now(), 1L)).thenReturn(true);

        UserReservationRequest userReservationRequest = new UserReservationRequest(LocalDate.now(), 1L, 1L);
        assertThatThrownBy(() -> reservationService.add(1L, userReservationRequest))
                .isInstanceOf(DuplicateException.class);

        verify(timeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).existsByDateAndTimeId(LocalDate.now(), 1L);
    }

    @DisplayName("예약 추가 시 존재하지 않는 시간 아이디를 조회하려고 할 때 예외를 발생시킨다")
    @Test
    void exception_invalid_time_id() {
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(2L)).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        UserReservationRequest userReservationRequest = new UserReservationRequest(LocalDate.now(), 2L, 1L);
        assertThatThrownBy(() -> reservationService.add(1L, userReservationRequest))
                .isInstanceOf(InvalidIdException.class);

        verify(timeRepository, times(1)).findById(2L);
    }

    @DisplayName("예약 추가 시 존재하지 않는 테마 아이디를 조회하려고 할 때 예외를 발생시킨다")
    @Test
    void exception_invalid_theme_id() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(themeRepository.findById(2L)).thenReturn(Optional.empty());

        UserReservationRequest userReservationRequest = new UserReservationRequest(LocalDate.now(), 1L, 2L);
        assertThatThrownBy(() -> reservationService.add(1L, userReservationRequest))
                .isInstanceOf(InvalidIdException.class);

        verify(timeRepository, times(1)).findById(1L);
        verify(themeRepository, times(1)).findById(2L);
    }

    @DisplayName("예약 추가 시 존재하지 않는 회원 아이디를 조회하려고 할 때 예외를 발생시킨다")
    @Test
    void exception_invalid_member_id() {
        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        UserReservationRequest userReservationRequest = new UserReservationRequest(LocalDate.now(), 1L, 1L);
        assertThatThrownBy(() -> reservationService.add(2L, userReservationRequest))
                .isInstanceOf(InvalidIdException.class);

        verify(memberRepository, times(1)).findById(2L);
    }

    @DisplayName("관리자가 예약 추가 시 현재보다 과거 시간인 경우 예외를 발생시킨다")
    @Test
    void exception_admin_time_before() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().minusHours(1));
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addByAdmin(adminReservationRequest))
                .isInstanceOf(InvalidTimeException.class);

        verify(timeRepository, times(1)).findById(1L);
    }

    @DisplayName("관리자가 예약 추가 시 동일 일자와 시간에 예약이 존재하는 경우 예외를 발생시킨다")
    @Test
    void exception_admin_not_available() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Member member = new Member(1L, "test", "test@test.com", "password", Role.USER);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(reservationRepository.existsByDateAndTimeId(LocalDate.now(), 1L)).thenReturn(true);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addByAdmin(adminReservationRequest))
                .isInstanceOf(DuplicateException.class);

        verify(timeRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).existsByDateAndTimeId(LocalDate.now(), 1L);
    }

    @DisplayName("예약 삭제 시 예약 아이디가 존재하지 않는 경우 예외를 발생시킨다")
    @Test
    void exception_invalid_id() {
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(InvalidIdException.class);

        verify(reservationRepository, times(1)).findById(1L);
    }
}
