package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@DataJpaTest
class ReservationTest {

    private Member member;
    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        member = Member.withDefaultRole("홍길동", "hong@example.com", "password");
        theme = Theme.of("테마명", "테마 설명", "thumbnail.jpg");
        time = ReservationTime.from(LocalTime.of(13, 0));
    }

    private Theme defaultTheme = Theme.of("테마", "설명", "썸네일");
    private Member defaultMember = Member.withRole("member", "member@naver.com", "1234", MemberRole.MEMBER);
    private Clock clock = Clock.systemDefaultZone();

    @Test
    void 새_예약의_id_필드는_null이다() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime later = LocalTime.now().plusMinutes(5);
        ReservationTime rt = ReservationTime.from(later);
        Reservation reservation = Reservation.of(today, rt, defaultTheme, defaultMember, LocalDateTime.now(clock));

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
                            () -> Reservation.of(null, reservationTime, theme, member, LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.of(localDate, null, theme, member, LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(
                            () -> Reservation.of(localDate, reservationTime, null, member,
                                    LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.of(null, reservationTime, theme, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.of(localDate, null, theme, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.of(localDate, reservationTime, null, member,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
            softly.assertThatThrownBy(() -> Reservation.of(localDate, reservationTime, theme, null,
                            LocalDateTime.now(clock)))
                    .isInstanceOf(NullPointerException.class);
        });
    }

    @Test
    void 예약_시간_검증_실패() {
        // given
        LocalDate date = LocalDate.now().minusDays(1);
        LocalDateTime currentDateTime = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> 
            Reservation.of(date, time, theme, member, currentDateTime)
        )
        .isInstanceOf(ReservationException.class)
        .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }
}
