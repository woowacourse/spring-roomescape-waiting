package roomescape.reservation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.util.SystemLocalDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.exception.NotReservationOwnerException;
import roomescape.reservation.application.exception.ReservationAlreadyExistsException;
import roomescape.reservation.application.exception.UnexpectedReservationStatusException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingReservationRequest;

@ExtendWith(MockitoExtension.class)
public class WaitingReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private WaitingReservationService waitingReservationService;

    @Test
    @DisplayName("같은 날짜, 시간, 테마에 대해 중복 대기 예약을 시도하면 예외가 발생한다")
    void validateDuplicatedWaitingReservationTest() {
        // given
        LocalDate date = SystemLocalDateTime.nowDate().plusDays(3);
        long timeId = 1L;
        long themeId = 1L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "theme", "description", "thumbnail");
        Member member = new Member(memberId, "name", "email@email.com", "password", Role.USER);

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.alreadyExists(date, time, theme, member)).thenReturn(true);

        WaitingReservationRequest request = new WaitingReservationRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> waitingReservationService.createWaitingReservation(request, member))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void createWaitingReservationTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        WaitingReservationRequest request = new WaitingReservationRequest(date, timeId, themeId);

        LocalDateTime createdAt = SystemLocalDateTime.now();

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                createdAt);

        List<Reservation> waitings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            waitings.add(new Reservation((long) i, date, time, theme, member, ReservationStatus.WAITING,
                    createdAt.minusDays(i + 1)));
        }

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.countReservationsBefore(date, time, theme, createdAt)).thenReturn(3);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponse response = waitingReservationService.createWaitingReservation(request, member);

        // then
        assertThat(response.id()).isEqualTo(99L);
    }

    @DisplayName("예약 대기를 취소할 수 있다.")
    @Test
    void deleteWaitingReservationTest() {
        // given
        LocalDate date = SystemLocalDateTime.nowDate().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);

        // 취소할 예약
        Reservation targetReservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                SystemLocalDateTime.now().minusDays(3));
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(targetReservation));

        // when
        waitingReservationService.deleteWaitingReservation(99L, member);

        // then
        verify(reservationRepository).delete(targetReservation);
    }

    @DisplayName("예약 대기의 주인이 아닐 경우 예약 대기 예외가 발생한다.")
    @Test
    void deleteWaitingReservationFailedTest_InvalidUser() {
        // given
        LocalDate date = SystemLocalDateTime.nowDate().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);
        Member otherMember = new Member(memberId + 1, "관리자", "email@email.com", "pw", Role.USER);

        // 취소할 예약
        Reservation targetReservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                SystemLocalDateTime.now().minusDays(3));
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(targetReservation));

        // when & then
        assertThatThrownBy(
                () -> waitingReservationService.deleteWaitingReservation(targetReservation.getId(), otherMember))
                .isInstanceOf(NotReservationOwnerException.class)
                .hasMessage("예약의 주인이 아닙니다.");
    }

    @DisplayName("관리자가 예약 대기 목록을 조회할 수 있다.")
    @Test
    void getWaitingReservationsTest() {
        // given
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            reservations.add(new Reservation(99L + i, SystemLocalDateTime.nowDate(), time, theme, member,
                    ReservationStatus.WAITING,
                    SystemLocalDateTime.now().minusDays(i + 1)));
        }

        when(reservationRepository.findAllByStatus(ReservationStatus.WAITING)).thenReturn(reservations);

        // when
        List<AdminWaitingReservationResponse> waitingReservations = waitingReservationService.getWaitingReservations();

        // then
        assertThat(waitingReservations.size()).isEqualTo(3);
    }


    @DisplayName("대기를 거절하려는 예약의 id가 대기 상태가 아닐 경우, 예외가 발생한다.")
    @Test
    void denyWaitingReservationTest_notWaitingReservation() {
        // given
        LocalDate date = SystemLocalDateTime.nowDate().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;
        long reservationId = 99L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);
        Reservation reservation = new Reservation(reservationId, date, time, theme, member, ReservationStatus.CONFIRMED,
                SystemLocalDateTime.now());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        assertThatThrownBy(() -> waitingReservationService.denyWaitingReservation(reservationId))
                .isInstanceOf(UnexpectedReservationStatusException.class);
    }

    @DisplayName("관리자가 회원의 대기를 거절할 수 있다.")
    @Test
    void denyWaitingReservationTest() {
        // given
        LocalDate date = SystemLocalDateTime.nowDate().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;
        long reservationId = 99L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);
        Reservation reservation = new Reservation(reservationId, date, time, theme, member, ReservationStatus.WAITING,
                SystemLocalDateTime.now());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        waitingReservationService.denyWaitingReservation(reservationId);

        // then
        verify(reservationRepository).delete(reservation);
    }

}
