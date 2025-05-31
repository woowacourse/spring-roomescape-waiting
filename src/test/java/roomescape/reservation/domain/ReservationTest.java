package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.domain.Email;
import roomescape.member.domain.Password;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private LocalDate now = LocalDate.now();

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = null;
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, new ReservationSlot(date, reservationTime, theme));
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = LocalDate.of(2025, 4, 24);
        final ReservationTime reservationTime = null;
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, new ReservationSlot(date, reservationTime, theme));
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, new ReservationSlot(date, reservationTime, theme));
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 예약_상태를_대기에서_확정으로_변경한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");
        final Reservation reservation = new Reservation(member, new ReservationSlot(date, reservationTime, theme),
                ReservationStatus.WAITING);

        // when
        reservation.confirmReservation();

        // then
        Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 이미_확정된_예약의_상태를_변경하려고_하면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");
        final Reservation reservation = new Reservation(member, new ReservationSlot(date, reservationTime, theme),
                ReservationStatus.CONFIRMED);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.confirmReservation())
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void 이미_취소된_예약의_상태를_변경하려고_하면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");
        final Reservation reservation = new Reservation(member, new ReservationSlot(date, reservationTime, theme),
                ReservationStatus.CANCELED);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.confirmReservation())
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void 예약_상태를_취소로_변경한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");
        final Reservation reservation = new Reservation(member, new ReservationSlot(date, reservationTime, theme));

        // when
        reservation.cancelReservation();

        // then
        Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    void 이미_취소된_예약을_다시_취소하려고_하면_예외가_발생한다() {
        // given
        final Member member = new Member(
                new MemberName("이스트"),
                new Email("east@email.com"),
                new Password("1234"),
                Role.ADMIN);
        final LocalDate date = now.plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");
        final Reservation reservation = new Reservation(member, new ReservationSlot(date, reservationTime, theme),
                ReservationStatus.CANCELED);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.cancelReservation())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
