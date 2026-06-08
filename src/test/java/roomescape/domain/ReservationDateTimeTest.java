package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReservationDateTime 값 객체 단위 테스트.
 *
 * <p>보호 대상: "내 시점이 기준 시각보다 이전이거나 같은가"라는 순수 비교의 정확성, 특히 경계.
 * 이 비교는 예전에 FutureOnlyPolicy 안에 묻혀 있어 경계를 보려면 정책을 거쳐야 했다. 값 객체로 끌어내면서 경계 정밀도(같은 순간은 포함된다 등)를 DB·스프링·시계·정책 없이 입력만으로
 * 검증한다.
 *
 * <p>"이 결과를 위반으로 볼지"와 "지금을 무엇으로 볼지(Clock)"는 정책의 책임이라 여기서 다루지 않는다.
 */
class ReservationDateTimeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 5, 13);
    private static final LocalDateTime MOMENT = DAY.atTime(LocalTime.of(12, 0));

    @Test
    @DisplayName("기준보다 이전 날짜면 이전이거나 같다")
    void 이전_날짜() {
        assertThat(ReservationDateTime.of(DAY.minusDays(1), LocalTime.of(12, 0)).startsAtOrBefore(MOMENT)).isTrue();
    }

    @Test
    @DisplayName("같은 날 이전 시각이면 이전이거나 같다 (시각까지 비교한다)")
    void 같은날_이전_시각() {
        assertThat(ReservationDateTime.of(DAY, LocalTime.of(10, 0)).startsAtOrBefore(MOMENT)).isTrue();
    }

    @Test
    @DisplayName("정확히 같은 순간은 이전이거나 같다 (경계)")
    void 동일_순간() {
        assertThat(ReservationDateTime.of(DAY, LocalTime.of(12, 0)).startsAtOrBefore(MOMENT)).isTrue();
    }

    @Test
    @DisplayName("같은 날 이후 시각이면 이전이거나 같지 않다")
    void 같은날_이후_시각() {
        assertThat(ReservationDateTime.of(DAY, LocalTime.of(15, 0)).startsAtOrBefore(MOMENT)).isFalse();
    }

    @Test
    @DisplayName("기준보다 이후 날짜면 이전이거나 같지 않다")
    void 이후_날짜() {
        assertThat(ReservationDateTime.of(DAY.plusDays(1), LocalTime.of(0, 1)).startsAtOrBefore(MOMENT)).isFalse();
    }
}
