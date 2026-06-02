package roomescape.reservation.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private ReservationTime time;
    private Theme theme;
    private Member member;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.restore(1L, "테마1", "설명", "https://image.com");
        member = Member.restore(1L, "현미밥", "test@test.com", "1234", Role.USER);
        futureDate = LocalDate.now().plusDays(1);
    }

    @Test
    @DisplayName("정상 예약 생성")
    void 정상_예약_생성() {
        Reservation reservation = Reservation.of(member, futureDate, time, theme);

        assertThat(reservation.getMember()).isEqualTo(member);
        assertThat(reservation.getDate()).isEqualTo(futureDate);
        assertThat(reservation.getTime()).isEqualTo(time);
        assertThat(reservation.getTheme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("멤버가 null이면 예외 발생")
    void 멤버_null_예외() {
        assertThatThrownBy(() -> Reservation.of(null, futureDate, time, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("날짜가 null이면 예외 발생")
    void 날짜_null_예외() {
        assertThatThrownBy(() -> Reservation.of(member, null, time, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("과거 날짜면 예외 발생")
    void 과거_날짜_예외() {
        assertThatThrownBy(() -> Reservation.of(member, LocalDate.now().minusDays(1), time, theme))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("시간이 null이면 예외 발생")
    void 시간_null_예외() {
        assertThatThrownBy(() -> Reservation.of(member, futureDate, null, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테마가 null이면 예외 발생")
    void 테마_null_예외() {
        assertThatThrownBy(() -> Reservation.of(member, futureDate, time, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("과거 예약이면 isPast가 true를 반환한다")
    void isPast_과거면_true() {
        Reservation past = Reservation.restore(1L, member, LocalDate.now().minusDays(1), time, theme);
        assertThat(past.isPast()).isTrue();
    }

    @Test
    @DisplayName("미래 예약이면 isPast가 false를 반환한다")
    void isPast_미래면_false() {
        Reservation future = Reservation.restore(1L, member, LocalDate.now().plusDays(1), time, theme);
        assertThat(future.isPast()).isFalse();
    }
}
