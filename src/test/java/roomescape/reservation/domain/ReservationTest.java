package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.error.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private Theme defaultTheme = new Theme(1L, "테마", "설명", "썸네일");
    private Member defaultMember = new Member(1L, "member", "member@naver.com", "1234",
            MemberRole.MEMBER.name());

    @Test
    void 예약_시간이_현재_이후면_객체가_정상_생성된다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteLater = LocalTime.now().plusMinutes(1);
        ReservationTime futureTime = new ReservationTime(1L, oneMinuteLater);

        // when
        // then
        assertThatCode(() ->
                new Reservation(today, futureTime, defaultTheme, defaultMember)
        ).doesNotThrowAnyException();
    }

    @Test
    void 예약_시간이_현재_이전이면_ReservationException이_발생한다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime oneMinuteBefore = LocalTime.now().minusMinutes(1);
        ReservationTime pastTime = new ReservationTime(1L, oneMinuteBefore);

        // when
        // then
        assertThatThrownBy(() ->
                new Reservation(today, pastTime, defaultTheme, defaultMember)
        )
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }

    @Test
    void 새_예약의_id_필드는_null이다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime later = LocalTime.now().plusMinutes(5);
        ReservationTime rt = new ReservationTime(1L, later);
        Reservation reservation = new Reservation(today, rt, defaultTheme, defaultMember);

        // when
        // then
        assertThat(reservation.getId()).isNull();
    }

    @Test
    void id_필드를_제외한_필드가_null이면_예외처리() {
        // given
        LocalDate localDate = LocalDate.of(2999, 1, 1);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("test", "test", "test");
        Member member = new Member(1L, "member", "member@naver.com", "1234", MemberRole.MEMBER.name());
        // when
        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(
                            () -> new Reservation(null, null, reservationTime, theme, member, ReservationStatus.BOOKED))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> new Reservation(null, localDate, null, theme, member, ReservationStatus.BOOKED))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> new Reservation(null, localDate, reservationTime, null, member, ReservationStatus.BOOKED))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> new Reservation(null, reservationTime, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> new Reservation(localDate, null, theme, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> new Reservation(localDate, reservationTime, null, member))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> new Reservation(localDate, reservationTime, theme, null))
                    .isInstanceOf(NullPointerException.class);
        });
    }
}
