package roomescape.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.waiting.ReservationWaitingRank;
import roomescape.domain.reservation.waiting.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;

class ReservationQueryServiceTest {

    @Mock
    private JpaReservationRepository reservationRepository;

    @Mock
    private JpaReservationWaitingTicketRepository waitingTicketRepository;

    @InjectMocks
    private ReservationQueryService reservationQueryService;

    private Member member;
    private Theme theme;
    private ReservationTime reservationTime;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        member = new Member(1L, "홍길동", "hong@example.com", Role.USER, "password");
        theme = new Theme(1L, "공포 테마", "무서운 배경 설명", "image-url");
        reservationTime = new ReservationTime(1L, LocalTime.of(14, 0));
        reservation = new Reservation(1L, member, LocalDate.now(), reservationTime, theme, ReservationStatus.RESERVED);
    }

    @DisplayName("모든 예약 조회 테스트")
    @Test
    void findAllReservations() {
        // given
        List<Reservation> allReservations = List.of(reservation);
        when(reservationRepository.findAll()).thenReturn(allReservations);

        // when
        List<ReservationResponseDto> result = reservationQueryService.findAllReservations();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(reservation.getId());
        assertThat(result.get(0).date()).isEqualTo(reservation.getDate());
        assertThat(result.get(0).theme().id()).isEqualTo(theme.getId());
    }

    @DisplayName("조건별 예약 검색 테스트")
    @Test
    void searchReservationsBy() {
        // given
        long themeId = 1L;
        long memberId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now().plusDays(7);

        List<Reservation> foundReservations = List.of(reservation);
        when(reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberIdAndStatus(
                from, to, themeId, memberId, ReservationStatus.RESERVED))
                .thenReturn(foundReservations);

        // when
        List<ReservationResponseDto> result = reservationQueryService.searchReservationsBy(themeId, memberId, from, to);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(reservation.getId());
    }

    @DisplayName("예약 상태가 RESERVED인 예약 조회 테스트")
    @Test
    void findReservedReservations() {
        // given
        List<Reservation> reservedReservations = List.of(reservation);
        when(reservationRepository.findReservationsByStatus(ReservationStatus.RESERVED))
                .thenReturn(reservedReservations);

        // when
        List<ReservationResponseDto> result = reservationQueryService.findReservedReservations();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(reservation.getId());
        assertThat(result.get(0).status()).isEqualTo(ReservationStatus.RESERVED.getMessage());
    }

    @DisplayName("예약 대기 목록 조회 테스트")
    @Test
    void findAllReservationWaitings() {
        // given
        Reservation waitingReservation = new Reservation(2L, member, LocalDate.now().plusDays(1), 
                reservationTime, theme, ReservationStatus.WAITING);

        List<Reservation> waitingReservations = List.of(waitingReservation);
        when(reservationRepository.findReservationsByStatus(ReservationStatus.WAITING))
                .thenReturn(waitingReservations);

        // when
        List<ReservationResponseDto> result = reservationQueryService.findAllReservationWaitings();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(waitingReservation.getId());
        assertThat(result.get(0).status()).isEqualTo(ReservationStatus.WAITING.getMessage());
    }

    @DisplayName("내 예약 목록 조회 테스트 - 일반 예약")
    @Test
    void findMyReservations() {
        // given
        LoginInfo loginInfo = new LoginInfo(member);
        List<Reservation> myReservations = List.of(reservation);

        when(reservationRepository.findReservationsByMemberId(loginInfo.id()))
                .thenReturn(myReservations);

        // when
        List<MyReservationResponseDto> result = reservationQueryService.findMyReservations(loginInfo);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(reservation.getId());
        assertThat(result.get(0).theme()).isEqualTo(theme.getName());
        assertThat(result.get(0).statusMessage()).isEqualTo(ReservationStatus.RESERVED.getMessage());
    }

    @DisplayName("내 예약 목록 조회 테스트 - 대기 예약")
    @Test
    void findMyReservationsWithWaiting() {
        // given
        LoginInfo loginInfo = new LoginInfo(member);
        Reservation waitingReservation = new Reservation(2L, member, LocalDate.now().plusDays(1), 
                reservationTime, theme, ReservationStatus.WAITING);

        List<Reservation> myReservations = List.of(waitingReservation);
        LocalDateTime createdAt = LocalDateTime.now();
        ReservationWaitingTicket waitingTicket = new ReservationWaitingTicket() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public Reservation getReservation() { return waitingReservation; }
            @Override
            public LocalDateTime getCreatedAt() { return createdAt; }
        };

        ReservationWaitingRank rank = new ReservationWaitingRank(3);

        when(reservationRepository.findReservationsByMemberId(loginInfo.id()))
                .thenReturn(myReservations);
        when(waitingTicketRepository.findByReservationId(waitingReservation.getId()))
                .thenReturn(Optional.of(waitingTicket));
        when(waitingTicketRepository.countReservationWaitingsByThemeIdAndDateAndTimeIdAndCreatedAt(
                waitingReservation.getTheme().getId(),
                waitingReservation.getDate(),
                waitingReservation.getTime().getId(),
                createdAt))
                .thenReturn(rank);

        // when
        List<MyReservationResponseDto> result = reservationQueryService.findMyReservations(loginInfo);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(waitingReservation.getId());
        assertThat(result.get(0).theme()).isEqualTo(theme.getName());
        assertThat(result.get(0).statusMessage()).isEqualTo("3번째 예약대기");
    }
}
