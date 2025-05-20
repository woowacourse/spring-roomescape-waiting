package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private Theme defaultTheme = Theme.of("테마", "설명", "썸네일");
    private Member defaultMember = Member.builder()
            .name("김철수")
            .email("kim@example.com")
            .password(Password.createForMember("pass123"))
            .role(MemberRole.MEMBER)
            .build();

    @Test
    void 새_예약의_id_필드는_null이다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime later = LocalTime.now().plusMinutes(5);
        ReservationTime rt = ReservationTime.from(later);
        Reservation reservation = Reservation.booked(today, rt, defaultTheme, defaultMember);

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
        Member member = Member.builder()
                .name("김철수")
                .email("kim@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();

        // when
        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(
                            () -> Reservation.booked(null, reservationTime, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.booked(localDate, null, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.booked(localDate, reservationTime, null, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(null, reservationTime, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, null, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, reservationTime, null, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.booked(localDate, reservationTime, theme, null))
                    .isInstanceOf(NullPointerException.class);
        });
    }
}
