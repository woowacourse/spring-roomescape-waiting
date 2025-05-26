package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.RESERVATION_TIME_1;
import static roomescape.testFixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.application.dto.ReservationDto;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.AuthorizationException;
import roomescape.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @InjectMocks
    private WaitingService waitingService;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MemberService memberService;

    @DisplayName("대기 상태의 모든 예약을 조회한다")
    @Test
    void getAllWaitings() {
        // given
        Theme theme = mock(Theme.class);
        ReservationTime reservationTime1 = mock(ReservationTime.class);
        ReservationTime reservationTime2 = mock(ReservationTime.class);
        Member member = mock(Member.class);
        Reservation reservation1 = Reservation.of(1L, member, theme, LocalDate.now(), reservationTime1,
                Status.statusWithoutId(ReservationStatus.WAITING));
        Reservation reservation2 = Reservation.of(2L, member, theme, LocalDate.now(), reservationTime2,
                Status.statusWithoutId(ReservationStatus.WAITING));
        Mockito.when(reservationRepository.findByStatusStatus(ReservationStatus.WAITING))
                .thenReturn(List.of(reservation1, reservation2));

        // when
        List<ReservationDto> allWaitings = waitingService.getAllWaitings();

        // then
        assertThat(allWaitings).hasSize(2);
        assertThat(allWaitings.get(0).id()).isEqualTo(1L);
        assertThat(allWaitings.get(1).id()).isEqualTo(2L);
    }

    @DisplayName("몇 번째 대기인지 확인한다")
    @Test
    void countWaitingReservation() {
        // given
        Theme theme = mock(Theme.class);
        ReservationTime time = mock(ReservationTime.class);
        Status status = Status.statusWithoutId(LocalDateTime.of(2025, 1, 1, 10, 0, 0), ReservationStatus.WAITING);
        Reservation reservation = Reservation.of(
                1L,
                Mockito.mock(Member.class),
                theme,
                LocalDate.of(2025, 1, 1),
                time,
                status
        );

        Mockito.when(reservationRepository.countByReservationStatusOrderByCreatedAt(
                theme,
                LocalDate.of(2025, 1, 1),
                time,
                LocalDate.of(2025, 1, 1).atTime(10, 0)
        )).thenReturn(5L);

        // when
        long result = waitingService.countWaitingReservation(reservation);

        // then
        assertThat(result).isEqualTo(5L);
    }

    @DisplayName("대기자가 없으면 대기할 수 없다")
    @Test
    void notCountWaitingReservation() {
        // given
        Reservation reservation = mock(Reservation.class);
        Mockito.when(reservation.isWaiting()).thenReturn(false);

        // then
        assertThatThrownBy(() -> waitingService.countWaitingReservation(reservation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기 상태가 아닙니다.");
    }

    @DisplayName("예약자가 없다면 대기를 예약으로 바꾼다")
    @Test
    void acceptReserve() {
        // given
        Long reservationId = 1L;
        Reservation reservation = Reservation.of(
                reservationId,
                MEMBER1_ADMIN,
                THEME_1,
                LocalDate.of(2025, 1, 1),
                RESERVATION_TIME_1,
                Status.statusWithoutId(LocalDateTime.of(2025, 1, 1, 10, 0, 0), ReservationStatus.WAITING)
        );
        doReturn(Optional.of(reservation)).when(reservationRepository).findById(1L);
        doReturn(false).when(reservationRepository)
                .existsByDateAndTimeIdAndThemeIdAndStatusStatus(
                        reservation.getDate(),
                        RESERVATION_TIME_1.getId(),
                        THEME_1.getId(),
                        ReservationStatus.RESERVED
                );

        // when
        waitingService.acceptReserve(reservationId);

        // then
        assertThat(reservation.isWaiting()).isFalse();
    }

    @DisplayName("이미 예약된 기록이 있으면 대기 상태를 변경할 수 없다")
    @Test
    void rejectReserve() {
        // given
        Long reservationId = 1L;
        Reservation reservation = Reservation.of(
                reservationId,
                Mockito.mock(Member.class),
                Mockito.mock(Theme.class),
                LocalDate.of(2025, 1, 1),
                Mockito.mock(ReservationTime.class),
                Status.statusWithoutId(LocalDateTime.of(2025, 1, 1, 10, 0, 0), ReservationStatus.CANCELED)
        );
        doReturn(Optional.of(reservation)).when(reservationRepository).findById(1L);

        // when
        // then
        assertThatThrownBy(() -> waitingService.acceptReserve(reservationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 예약은 대기 상태가 아닙니다.");
    }

    @DisplayName("관리자가 다른 사람의 예약을 삭제할 수 있다")
    @Test
    void deleteWaitingByAdmin() {
        // given
        Member admin = mock(Member.class);
        Reservation reservation = mock(Reservation.class);

        Mockito.when(admin.isAdmin()).thenReturn(true);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(admin);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        // when
        waitingService.deleteWaiting(100L, 1L);

        // then
        Mockito.verify(reservation).deleteSelf();
        Mockito.verify(reservationRepository).delete(reservation);
    }

    @DisplayName("본인은 자신의 예약을 삭제할 수 있다")
    @Test
    void deleteWaitingBySelf() {
        // given
        Member user = mock(Member.class);
        Reservation reservation = mock(Reservation.class);

        Mockito.when(user.isAdmin()).thenReturn(false);
        Mockito.when(user.isNotEqual(Mockito.any())).thenReturn(false);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(user);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        Mockito.when(reservation.getMember()).thenReturn(mock(Member.class));

        // when
        waitingService.deleteWaiting(100L, 1L);

        // then
        Mockito.verify(reservation).deleteSelf();
        Mockito.verify(reservationRepository).delete(reservation);
    }

    @DisplayName("다른 사람의 예약은 삭제할 수 없다")
    @Test
    void deleteWaitingUnauthorized() {
        // given
        Member user = mock(Member.class);
        Reservation reservation = mock(Reservation.class);

        Mockito.when(user.isAdmin()).thenReturn(false);
        Mockito.when(user.isNotEqual(Mockito.any())).thenReturn(true);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(user);
        Mockito.when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        Mockito.when(reservation.getMember()).thenReturn(mock(Member.class));

        // when
        // then
        assertThatThrownBy(() -> waitingService.deleteWaiting(100L, 1L))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
    }

    @DisplayName("예약이 존재하지 않으면 예외를 던진다")
    @Test
    void deleteWaitingNotFound() {
        // given
        Member member = mock(Member.class);
        Mockito.when(memberService.getMemberEntityById(1L)).thenReturn(member);
        Mockito.when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> waitingService.deleteWaiting(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약 id가 존재하지 않습니다. id: 999");
    }
}
