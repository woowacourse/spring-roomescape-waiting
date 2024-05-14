package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;

class ReservationTest {

    @Test
    @DisplayName("id, reservationDate, reservationTime,Theme,Member 을 통해 도메인을 생성한다.")
    void create_with_id_name_reservationDate_reservationTime() {
        assertThatCode(() ->
                new Reservation(
                        null,
                        ReservationDate.from("2024-04-03"),
                        ReservationTime.from(null, "10:00"),
                        ThemeFixture.getDomain(),
                        MemberFixture.getDomain()
                ))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("id, 문자열들 을 통해 도메인을 생성한다.")
    void create_with_factory_method() {
        assertThatCode(() ->
                Reservation.from(
                        null,
                        "2024-04-03",
                        ReservationTime.from(null, "10:00"),
                        ThemeFixture.getDomain(),
                        MemberFixture.getDomain()))
                .doesNotThrowAnyException();
    }

    private static Stream<Arguments> maskingDateAndTime() {
        return Stream.of(
                Arguments.arguments(Reservation.from(null, "2024-04-01", ReservationTime.from(null, "10:00"),
                        ThemeFixture.getDomain(), MemberFixture.getDomain())),
                Arguments.arguments(Reservation.from(null, "2024-04-02", ReservationTime.from(null, "09:59"),
                        ThemeFixture.getDomain(), MemberFixture.getDomain()))
        );
    }

    @ParameterizedTest
    @MethodSource("maskingDateAndTime")
    @DisplayName("날짜가 이전이거나 날짜가 같을 때 시간이 이전이면 참을 반환한다.")
    void return_true_when_date_is_before_or_date_is_equal_and_time_is_before(final Reservation reservation) {
        final boolean result = reservation.isBefore(LocalDate.parse("2024-04-02"), LocalTime.parse("10:00"));
        assertThat(result).isTrue();
    }

}
