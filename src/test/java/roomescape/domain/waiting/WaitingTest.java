package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingTest {

    @Test
    void 자신의_예약에_대한_예약_대기_생성_시_예외_발생() {
        // given
        Member member = new Member("name", "email@test.com", "password123", Role.ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = new Theme("테마", "주어진 사용자의 예약인지 확인합니다.", "썸네일");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Reservation reservation = new Reservation(date, reservationTime, theme, member);

        // when, then
        assertThatThrownBy(() -> new Waiting(member, reservation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 대기를_예약으로_승인() {
        Member member1 = new Member("제이", "email@test.com", "password123", Role.ADMIN);
        Member member2 = new Member("아톰", "email@test.com", "password123", Role.ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = new Theme("테마", "테마에 대한 설명입니다.", "썸네일");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Reservation reservation = new Reservation(date, reservationTime, theme, member1);
        Waiting waiting = new Waiting(member2, reservation);

        // when
        waiting.approve();

        // then
        Member reservationOwner = reservation.getMember();
        assertThat(reservationOwner).isEqualTo(member2);
    }

    @Test
    void 대기의_소유자가_아닌지_검증() {
        Member member1 = new Member("제이", "email@test.com", "password123", Role.ADMIN);
        Member member2 = new Member("아톰", "email@test.com", "password123", Role.ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = new Theme("테마", "테마에 대한 설명입니다.", "썸네일");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Reservation reservation = new Reservation(date, reservationTime, theme, member1);
        Waiting waiting = new Waiting(member2, reservation);

        // when, then
        Assertions.assertAll(
                () -> assertThat(waiting.isNotOwner(member1)).isTrue(),
                () -> assertThat(waiting.isNotOwner(member2)).isFalse()
        );
    }
}
