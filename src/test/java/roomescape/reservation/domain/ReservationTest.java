package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    @DisplayName("주어진 날짜와 시간이 미래라면 정상적으로 생성된다.")
    @Test
    void of1() {
        // given
        final LocalDateTime currentDateTime = LocalDateTime.of(2024, 12, 1, 12, 0);
        final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 29));
        final ReservationDate date = ReservationDate.fromQuery(currentDateTime.toLocalDate());
        final Theme theme = new Theme("테마 이름", "테마 설명", "http://example.com/image.jpg");

        // when
        final Reservation actual = Reservation.of(date, member, reservationTime, theme, ReservationStatus.PENDING,
                currentDateTime);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.getDate()).isEqualTo(date);
            s.assertThat(actual.getMember()).isEqualTo(member);
            s.assertThat(actual.getReservationTime()).isEqualTo(reservationTime);
            s.assertThat(actual.getTheme()).isEqualTo(theme);
            s.assertThat(actual.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);
            s.assertThat(actual.getId()).isNull(); // ID는 null로 초기화되어야 함
        });
    }

    @DisplayName("주어진 날짜와 시간이 과거라면 예외가 발생한다.")
    @Test
    void of2() {
        // given
        final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 30, 12, 30);
        final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 29));
        final ReservationDate date = ReservationDate.fromQuery(currentDateTime.toLocalDate());
        final Theme theme = new Theme("테마 이름", "테마 설명", "http://example.com/image.jpg");

        // when & then
        assertThatThrownBy(() -> {
            Reservation.of(date, member, reservationTime, theme, ReservationStatus.PENDING, currentDateTime);
        }).isInstanceOf(ReservationPastTimeException.class);
    }

    @DisplayName("Reservation의 상태가 Waiting이라면, true 아니라면 false를 반환한다.")
    @ParameterizedTest
    @CsvSource(value = {"WAITING:true", "PENDING:false"}, delimiter = ':')
    void isWaiting(final ReservationStatus status, final boolean expected) {
        // given
        final LocalDateTime currentDateTime = LocalDateTime.of(2024, 12, 1, 12, 0);
        final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 29));
        final ReservationDate date = ReservationDate.fromQuery(currentDateTime.toLocalDate());
        final Theme theme = new Theme("테마 이름", "테마 설명", "http://example.com/image.jpg");

        final Reservation reservation = Reservation.of(date, member, reservationTime, theme, status, currentDateTime);

        // when
        final boolean actual = reservation.isWaiting();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("Reservation의 상태를 Pending 상태로 전환합니다.")
    @Test
    void pending() {
        // given
        final LocalDateTime currentDateTime = LocalDateTime.of(2024, 12, 1, 12, 0);
        final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 29));
        final ReservationDate date = ReservationDate.fromQuery(currentDateTime.toLocalDate());
        final Theme theme = new Theme("테마 이름", "테마 설명", "http://example.com/image.jpg");

        final Reservation reservation = Reservation.of(date, member, reservationTime, theme, ReservationStatus.WAITING, currentDateTime);

        // when
        reservation.pending();

        // then
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
