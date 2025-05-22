package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.admin.AdminWaitingReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.WaitingReservationRequest;
import roomescape.exception.reservation.ReservationAlreadyExistsException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationStatusRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationStatusRepository reservationStatusRepository;

    @InjectMocks
    private ReservationService reservationService;

    private final ReservationStatus status = new ReservationStatus(1L);

    @DisplayName("예약 시간이 존재하지 않으면 예약을 생성할 수 없다")
    @Test
    void timeNotFound() {
        // given
        LocalDate today = LocalDate.now();
        Long timeId = 1L;
        Long themeId = 1L;

        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        when(timeRepository.findById(timeId)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호",
                Role.ADMIN)))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @DisplayName("테마가 존재하지 않으면 예약을 생성할 수 없다")
    @Test
    void themeNotFound() {
        // given
        LocalDate today = LocalDate.now();
        long timeId = 1L;
        long themeId = 1L;
        String name = "에드";
        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(new ReservationTime(timeId, LocalTime.now().plusHours(1))));

        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());
        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN)))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("이미 예약이 존재하는 날짜, 시간에는 예약을 생성할 수 없다")
    @Test
    void alreadyExists() {
        // given
        LocalDate today = LocalDate.now();
        long timeId = 1L;
        long themeId = 1L;
        String name = "에드";
        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        ReservationTime time = new ReservationTime(timeId, LocalTime.now().plusHours(1));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");

        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(time));

        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeAndTheme(today, time, theme)).thenReturn(true);

        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN)))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @DisplayName("관리자가 정상적으로 예약을 생성할 수 있다")
    @Test
    void createByAdminSuccess() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        AdminReservationRequest request = new AdminReservationRequest(date, timeId, themeId, memberId);

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", roomescape.domain.enums.Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member, status);
        ReservationStatus reservationStatus = new ReservationStatus(1L);

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationStatusRepository.save(any(ReservationStatus.class))).thenReturn(reservationStatus);

        // when
        ReservationResponse response = reservationService.createByAdmin(request);

        // then
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.member().name()).isEqualTo("관리자");
        assertThat(response.theme().name()).isEqualTo("SF 테마");
        assertThat(response.date()).isEqualTo(date);
    }

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void createWaitingReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        WaitingReservationRequest request = new WaitingReservationRequest(date, timeId, themeId);

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", roomescape.domain.enums.Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member, status);
        ReservationStatus reservationStatus = new ReservationStatus(1L);

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.countByDateAndTimeAndTheme(date, time, theme)).thenReturn(3L);
        when(reservationStatusRepository.save(any(ReservationStatus.class))).thenReturn(reservationStatus);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponse response = reservationService.createWaitingReservation(request, member);

        // then
        assertThat(response.id()).isEqualTo(99L);
    }

    @Test
    @DisplayName("예약 대기를 취소할 수 있다.")
    void deleteWaitingReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;
        long priority = 3L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);

        // 취소할 예약
        ReservationStatus targetStatus = new ReservationStatus(3L);
        Reservation targetReservation = new Reservation(99L, date, time, theme, member, targetStatus);

        when(reservationRepository.findById(99L)).thenReturn(Optional.of(targetReservation));
        doNothing().when(reservationRepository).updateAllWaitingReservationsAfterPriority(date, time, theme, priority);

        // when
        reservationService.deleteWaitingReservation(99L, member);

        // then
        // TODO: 순위 변동 사항은 repository의 메서드 책임이니까 그 부분은 mocking 하고 결과는 verify로만 검증을 할까?
        verify(reservationRepository).delete(targetReservation);
        verify(reservationRepository).updateAllWaitingReservationsAfterPriority(date, time, theme, priority);
    }

    @Test
    @DisplayName("관리자가 예약 대기 목록을 조회할 수 있다.")
    void getWaitingReservationsTest() {
        // given
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;
        long priority = 3L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            reservations.add(new Reservation(99L + i, LocalDate.now(), time, theme, member,
                    new ReservationStatus(priority)));
        }

        when(reservationRepository.findAllWaiting()).thenReturn(reservations);

        // when
        List<AdminWaitingReservationResponse> waitingReservations = reservationService.getWaitingReservations();

        // then
        assertThat(waitingReservations.size()).isEqualTo(3);
    }
}
