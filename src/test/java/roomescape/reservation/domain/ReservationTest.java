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
        ReservationTime reservationTime = ReservationTime.from(later);

        RoomEscapeInformation info = RoomEscapeInformation.builder()
                .date(today)
                .time(reservationTime)
                .theme(defaultTheme)
                .build();

        Reservation reservation = Reservation.builder()
                .roomEscapeInformation(info)
                .member(defaultMember)
                .build();

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
            softly.assertThatThrownBy(() -> {
                RoomEscapeInformation info = RoomEscapeInformation.builder()
                        .date(null)
                        .time(reservationTime)
                        .theme(theme)
                        .build();
                Reservation.builder()
                        .roomEscapeInformation(info)
                        .member(member)
                        .build();
            }).isInstanceOf(NullPointerException.class);

            softly.assertThatThrownBy(() -> {
                RoomEscapeInformation info = RoomEscapeInformation.builder()
                        .date(localDate)
                        .time(null)
                        .theme(theme)
                        .build();
                Reservation.builder()
                        .roomEscapeInformation(info)
                        .member(member)
                        .build();
            }).isInstanceOf(NullPointerException.class);

            softly.assertThatThrownBy(() -> {
                RoomEscapeInformation info = RoomEscapeInformation.builder()
                        .date(localDate)
                        .time(reservationTime)
                        .theme(null)
                        .build();
                Reservation.builder()
                        .roomEscapeInformation(info)
                        .member(member)
                        .build();
            }).isInstanceOf(NullPointerException.class);

            softly.assertThatThrownBy(() -> {
                RoomEscapeInformation info = RoomEscapeInformation.builder()
                        .date(localDate)
                        .time(reservationTime)
                        .theme(theme)
                        .build();
                Reservation.builder()
                        .roomEscapeInformation(info)
                        .member(null)
                        .build();
            }).isInstanceOf(NullPointerException.class);

            softly.assertThatThrownBy(() -> {
                Reservation.builder()
                        .roomEscapeInformation(null)
                        .member(member)
                        .build();
            }).isInstanceOf(NullPointerException.class);
        });
    }

    @Test
    void 예약_시간이_현재_이후면_객체가_정상_생성된다() {
        // given
        LocalDate localDate = LocalDate.now();
        LocalTime oneMinuteLater = LocalTime.now().plusMinutes(1);
        ReservationTime futureTime = ReservationTime.from(oneMinuteLater);
        Theme theme = Theme.of("테마", "설명", "썸네일");
        Member member = Member.builder()
                .name("김철수")
                .email("kim@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();

        RoomEscapeInformation info = RoomEscapeInformation.builder()
                .date(localDate)
                .time(futureTime)
                .theme(theme)
                .build();

        // when
        Reservation reservation = Reservation.builder()
                .roomEscapeInformation(info)
                .member(member)
                .build();

        // then
        assertThat(reservation.getRoomEscapeInformation().getDate()).isEqualTo(localDate);
        assertThat(reservation.getRoomEscapeInformation().getTime()).isEqualTo(futureTime);
        assertThat(reservation.getRoomEscapeInformation().getTheme()).isEqualTo(theme);
        assertThat(reservation.getMember()).isEqualTo(member);
    }
}
