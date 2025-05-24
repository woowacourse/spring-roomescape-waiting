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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.exception.NotReservationOwnerException;
import roomescape.reservation.application.exception.ReservationInPastException;
import roomescape.reservation.application.exception.ReservationTimeNotFoundException;
import roomescape.reservation.application.exception.ThemeNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.reservation.presentation.dto.MemberReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.SearchConditionsRequest;
import roomescape.reservation.presentation.dto.WaitingReservationRequest;

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

    @InjectMocks
    private ReservationService reservationService;

    @DisplayName("예약 시간이 존재하지 않으면 예약을 생성할 수 없다")
    @Test
    void createFailed_timeNotFoundTest() {
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
    void createFailed_themeNotFoundTest() {
        // given
        LocalDate today = LocalDate.now();
        long timeId = 1L;
        long themeId = 1L;

        ReservationRequest request = new ReservationRequest(today, timeId, themeId);

        // when
        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(new ReservationTime(timeId, LocalTime.now().plusHours(1))));

        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());
        //then
        assertThatThrownBy(() -> reservationService.create(request, new Member(1L, "슬링키", "이메일", "비밀번호", Role.ADMIN)))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("예약 날짜가 오늘 이전이면 예외가 발생한다.")
    @Test
    void createFailed_createInPastTest() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(3);
        long timeId = 1L;
        long themeId = 1L;
        long memberId = 2L;

        ReservationRequest request = new ReservationRequest(pastDate, timeId, themeId);
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");

        when(timeRepository.findById(timeId)).thenReturn(
                Optional.of(new ReservationTime(timeId, LocalTime.now().plusHours(1))));

        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));

        // when & then
        assertThatThrownBy(() -> reservationService.create(request, member))
                .isInstanceOf(ReservationInPastException.class);
    }

    @DisplayName("예약을 생성할 수 있다.")
    @Test
    void createTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(3);
        long timeId = 1L;
        long themeId = 1L;
        long memberId = 2L;
        long reservationId = 99L;

        ReservationRequest request = new ReservationRequest(date, timeId, themeId);

        ReservationTime time = new ReservationTime(timeId, LocalTime.now().plusHours(1));
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");

        Reservation reservation = new Reservation(reservationId, date, time, theme, member, ReservationStatus.CONFIRMED,
                LocalDateTime.now());

        when(timeRepository.findById(timeId)).thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeAndTheme(date, time, theme)).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponse createdReservation = reservationService.create(request, member);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(createdReservation.id()).isEqualTo(reservationId);
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
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member, ReservationStatus.CONFIRMED,
                LocalDateTime.now());

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponse response = reservationService.createByAdmin(request);

        // then
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.member().name()).isEqualTo("관리자");
        assertThat(response.theme().name()).isEqualTo("SF 테마");
        assertThat(response.date()).isEqualTo(date);
    }

    @DisplayName("예약 목록을 검색할 수 있다.")
    @Test
    void getReservationsByConditionsTest() {
        // given
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(7);
        Long themeId = 1L;
        Long memberId = 1L;

        SearchConditionsRequest request = new SearchConditionsRequest(themeId, memberId, dateFrom, dateTo);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            reservations.add(new Reservation(
                    (long) i, dateFrom.plusDays(i), time, theme, member,
                    ReservationStatus.CONFIRMED, LocalDateTime.now()
            ));
        }

        when(reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                themeId, memberId, dateFrom, dateTo
        )).thenReturn(reservations);

        // when
        List<ReservationResponse> result = reservationService.getReservationsByConditions(request);

        // then
        Assertions.assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result.getFirst().theme().id()).isEqualTo(themeId);
            assertThat(result.getFirst().member().id()).isEqualTo(memberId);
            assertThat(result.getFirst().date()).isEqualTo(dateFrom);
        });
    }

    @DisplayName("회원에 대한 예약 목록을 조회한다.")
    @Test
    void getReservationByMemberTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "admin@a.com", "pw", Role.ADMIN);

        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            reservations.add(new Reservation(
                    (long) i, date.plusDays(i), time, theme, member,
                    ReservationStatus.CONFIRMED, LocalDateTime.now()
            ));
        }

        reservations.add(new Reservation(
                2L, date.plusDays(2), time, theme, member,
                ReservationStatus.WAITING, LocalDateTime.now()
        ));

        when(reservationRepository.findAllByMember(member)).thenReturn(reservations);

        // when
        List<MemberReservationResponse> result = reservationService.getReservationByMember(member);

        // then
        Assertions.assertAll(() -> {
            assertThat(result).hasSize(3);
            assertThat(result.getFirst().theme()).isEqualTo("SF 테마");
            assertThat(result.getFirst().date()).isEqualTo(date);
        });
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

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);
        Reservation reservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                LocalDateTime.now());

        List<Reservation> waitings = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            waitings.add(new Reservation((long) i, date, time, theme, member, ReservationStatus.WAITING,
                    LocalDateTime.now()));
        }

        when(timeRepository.findById(anyLong())).thenReturn(Optional.of(time));
        when(themeRepository.findById(anyLong())).thenReturn(Optional.of(theme));
        when(reservationRepository.findAllByDateAndThemeAndTime(date, theme, time)).thenReturn(waitings);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationResponse response = reservationService.createWaitingReservation(request, member);

        // then
        assertThat(response.id()).isEqualTo(99L);
    }

    @DisplayName("예약 대기를 취소할 수 있다.")
    @Test
    void deleteWaitingReservationTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.ADMIN);

        // 취소할 예약
        Reservation targetReservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                LocalDateTime.now().minusDays(3));
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(targetReservation));

        // when
        reservationService.deleteWaitingReservation(99L, member);

        // then
        verify(reservationRepository).delete(targetReservation);
    }

    @DisplayName("예약 대기의 주인이 아닐 경우 예약 대기 예외가 발생한다.")
    @Test
    void deleteWaitingReservationFailedTest_InvalidUser() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);
        Member otherMember = new Member(memberId + 1, "관리자", "email@email.com", "pw", Role.USER);

        // 취소할 예약
        Reservation targetReservation = new Reservation(99L, date, time, theme, member, ReservationStatus.WAITING,
                LocalDateTime.now().minusDays(3));
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(targetReservation));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteWaitingReservation(targetReservation.getId(), otherMember))
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
            reservations.add(new Reservation(99L + i, LocalDate.now(), time, theme, member, ReservationStatus.WAITING,
                    LocalDateTime.now().minusDays(i + 1)));
        }

        when(reservationRepository.findAllByStatus(ReservationStatus.WAITING)).thenReturn(reservations);

        // when
        List<AdminWaitingReservationResponse> waitingReservations = reservationService.getWaitingReservations();

        // then
        assertThat(waitingReservations.size()).isEqualTo(3);
    }

    @DisplayName("예약 아이디로 예약을 삭제한다.")
    @Test
    void deleteByIdTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;
        long reservationId = 99L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);

        Reservation targetReservation = new Reservation(reservationId, date, time, theme, member,
                ReservationStatus.WAITING,
                LocalDateTime.now().minusDays(3));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(targetReservation));

        // when
        reservationService.deleteById(reservationId);

        // then
        verify(reservationRepository).delete(targetReservation);
    }

    @DisplayName("에약 전체를 조회한다.")
    @Test
    void getAllTest() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        long timeId = 2L;
        long themeId = 2L;
        long memberId = 2L;

        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Theme theme = new Theme(themeId, "SF 테마", "미래", "url");
        Member member = new Member(memberId, "관리자", "email@email.com", "pw", Role.USER);

        List<Reservation> reservations = new ArrayList<>();
        for (long i = 90; i < 95; i++) {
            Reservation reservation = new Reservation(i, date, time, theme, member,
                    ReservationStatus.CONFIRMED,
                    LocalDateTime.now().minusDays(i));
            reservations.add(reservation);
        }

        when(reservationRepository.findAll()).thenReturn(reservations);

        // when
        List<ReservationResponse> result = reservationService.getAll();

        // then
        assertThat(result.size()).isEqualTo(5);
    }
}
