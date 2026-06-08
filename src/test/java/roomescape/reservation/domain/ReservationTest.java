package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private final Clock clock = Clock.fixed(
            LocalDate.now().atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );
    private final ReservationFactory factory = new ReservationFactory(clock);
    private final ReservationTime time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    private final Theme theme = Theme.restore(1L, "테마1", "설명", "https://image.com");
    private final LocalDate futureDate = LocalDate.now().plusDays(1);
    private final ReservationSlot slot = new ReservationSlot(futureDate, time, theme);

    @Test
    @DisplayName("이름이 null이면 예외 발생")
    void 이름_null_예외() {
        assertThatThrownBy(() -> factory.create(null, slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("이름이 공백이면 예외 발생")
    void 이름_공백_예외() {
        assertThatThrownBy(() -> factory.create("  ", slot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("날짜가 null이면 예외 발생")
    void 날짜_null_예외() {
        assertThatThrownBy(() -> factory.create("현미밥", new ReservationSlot(null, time, theme)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 필수입니다.");
    }

    @Test
    @DisplayName("과거 날짜면 예외 발생")
    void 과거_날짜_예외() {
        assertThatThrownBy(() -> factory.create("현미밥", new ReservationSlot(LocalDate.now().minusDays(1), time, theme)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("시간이 null이면 예외 발생")
    void 시간_null_예외() {
        assertThatThrownBy(() -> factory.create("현미밥", new ReservationSlot(futureDate, null, theme)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간은 필수입니다.");
    }

    @Test
    @DisplayName("테마가 null이면 예외 발생")
    void 테마_null_예외() {
        assertThatThrownBy(() -> factory.create("현미밥", new ReservationSlot(futureDate, time, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @Test
    @DisplayName("과거 예약이면 isPast가 true를 반환한다")
    void isPast_과거면_true() {
        Reservation past = Reservation.restore(1L, "현미밥",
                new ReservationSlot(LocalDate.now().minusDays(1), time, theme));
        assertThat(past.isPast(clock)).isTrue();
    }

    @Test
    @DisplayName("미래 예약이면 isPast가 false를 반환한다")
    void isPast_미래면_false() {
        Reservation future = Reservation.restore(1L, "현미밥",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        assertThat(future.isPast(clock)).isFalse();
    }

    @Test
    @DisplayName("예약하는 날짜가 과거면 예외를 반환한다")
    void 수정할_날짜가_과거면_예외() {
        Reservation reservation = factory.create("무빙", slot);
        assertThatThrownBy(() -> reservation.reschedule(LocalDate.now().minusDays(1), time, clock))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 시간으로 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("수정할 날짜로 예약을 변경한다")
    void 예약_날짜_수정() {
        Reservation reservation = factory.create("무빙", slot);
        Reservation validReservation = reservation.reschedule(LocalDate.now().plusDays(5), time, clock);
        assertThat(validReservation.getDate()).isEqualTo(LocalDate.now().plusDays(5));
    }
}
