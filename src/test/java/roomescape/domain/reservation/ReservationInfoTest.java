package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.fixture.ThemeFixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReservationInfoTest {

    @Test
    @DisplayName("id, reservationDate, reservationTime,Theme 을 통해 도메인을 생성한다.")
    void create_with_id_name_reservationDate_reservationTime() {
        assertThatCode(() ->
                new ReservationInfo(
                        ReservationDate.from("2024-04-03"),
                        ReservationTime.from("10:00"),
                        ThemeFixture.getDomain()
                )
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("id, 문자열들 을 통해 도메인을 생성한다.")
    void create_with_factory_method() {
        assertThatCode(() ->
                ReservationInfo.from(
                        "2024-04-03",
                        ReservationTime.from("10:00"),
                        ThemeFixture.getDomain()
                ))
                .doesNotThrowAnyException();
    }

    private static Stream<Arguments> maskingDateAndTime() {
        return Stream.of(
                Arguments.arguments(ReservationInfo.from("2024-04-01", ReservationTime.from("10:00"),
                        ThemeFixture.getDomain())),
                Arguments.arguments(ReservationInfo.from("2024-04-02", ReservationTime.from("09:59"),
                        ThemeFixture.getDomain()))
        );
    }

    @ParameterizedTest
    @MethodSource("maskingDateAndTime")
    @DisplayName("날짜가 이전이거나 날짜가 같을 때 시간이 이전이면 참을 반환한다.")
    void return_true_when_date_is_before_or_date_is_equal_and_time_is_before(final ReservationInfo reservationInfo) {
        final boolean result = reservationInfo.isBefore(LocalDate.parse("2024-04-02"), LocalTime.parse("10:00"));
        assertThat(result).isTrue();
    }

}
