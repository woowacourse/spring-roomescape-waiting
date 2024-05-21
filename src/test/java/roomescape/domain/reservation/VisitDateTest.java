package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VisitDateTest {

    private static Stream<Arguments> beforeDayMethod() {
        return Stream.of(
                Arguments.arguments("2024-05-08", "2024-05-07"),
                Arguments.arguments("2024-01-01", "2023-12-31")
        );
    }

    @ParameterizedTest(name = "{0} 의 하루 전은 {1} 이다")
    @MethodSource("beforeDayMethod")
    @DisplayName("방문일의 하루 전 날을 반환한다.")
    void return_before_1_day(final String reservationDate, final String beforeDate) {
        final VisitDate visitDate = VisitDate.from(reservationDate);
        final VisitDate beforeDayDate = visitDate.beforeDay();
        assertThat(beforeDayDate.asString()).isEqualTo(beforeDate);
    }

    private static Stream<Arguments> beforeWeekMethod() {
        return Stream.of(
                Arguments.arguments("2024-05-08", "2024-05-01"),
                Arguments.arguments("2024-05-02", "2024-04-25")
        );
    }

    @ParameterizedTest(name = "{0} 의 일주일 전은 {1} 이다")
    @MethodSource("beforeWeekMethod")
    @DisplayName("방문일의 일주일 전 날을 반환한다.")
    void return_before_1_week(final String reservationDate, final String beforeDate) {
        final VisitDate visitDate = VisitDate.from(reservationDate);
        final VisitDate beforeWeekDate = visitDate.beforeWeek();
        assertThat(beforeWeekDate.asString()).isEqualTo(beforeDate);
    }
}
