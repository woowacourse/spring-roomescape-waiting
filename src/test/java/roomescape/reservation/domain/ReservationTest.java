package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private Theme defaultTheme = Theme.of("테마", "설명", "썸네일");
    private Member defaultMember = Member.withRole("member", "member@naver.com", "1234", MemberRole.MEMBER);
    private Clock clock = Clock.systemDefaultZone();

    @Test
    void 새_예약의_id_필드는_null이다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime later = LocalTime.now().plusMinutes(5);
        ReservationTime rt = ReservationTime.from(later);
        Reservation reservation = Reservation.booked(today, rt, defaultTheme, defaultMember, LocalDateTime.now(clock));

        // when
        // then
        assertThat(reservation.getId()).isNull();
    }

    @Test
    void id_필드를_제외한_필드가_null이면_예외처리() {
        // given
        LocalDate localDate = LocalDate.of(2999, 1, 1);
        ReservationTime reservationTime = ReservationTime.from(LocalTime.of(11, 0));
        Theme theme = Theme.of("test", "test", "test");
        Member member = Member.withRole("member", "member@naver.com", "1234", MemberRole.MEMBER);
        // when
        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(
                            () -> Reservation.booked(null, reservationTime, theme, member, LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.booked(localDate, null, theme, member, LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.booked(localDate, reservationTime, null, member,
                                    LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(null, reservationTime, theme, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, null, theme, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, reservationTime, null, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, reservationTime, theme, null,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
        });
    }
}
