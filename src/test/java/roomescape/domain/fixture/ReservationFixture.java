package roomescape.domain.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class ReservationFixture {

    public static final LocalDateTime FIXED = LocalDateTime.of(2025, 1, 1, 12, 0);

    public static Stream<Arguments> invalidReservationConstructor() {
        return Stream.of(
                // 날짜 정보가 누락된 경우
                Arguments.of(
                        null,
                        ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefault(),
                        "예약 날짜 및 시간 정보는 비어있을 수 없습니다."
                ),
                // 테마 정보가 누락된 경우
                Arguments.of(
                        LocalDate.now().plusDays(1),
                        null,
                        ReservationTimeFixture.createDefault(),
                        "테마 정보는 비어있을 수 없습니다."
                ),
                // 예약 시간 정보가 누락된 경우
                Arguments.of(
                        LocalDate.now().plusDays(1),
                        ThemeFixture.createDefaultTheme(),
                        null,
                        "예약 날짜 및 시간 정보는 비어있을 수 없습니다."
                ),
                // 오늘보다 과거 날짜인 경우
                Arguments.of(
                        LocalDate.now().minusDays(1),
                        ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefault(),
                        "이전 날짜로 예약 할 수 없습니다."
                ),
                // 오늘과 날짜는 동일하지만 시간이 과거인 경우
                Arguments.of(
                        LocalDate.now(),
                        ThemeFixture.createDefaultTheme(),
                        new ReservationTime(LocalTime.now().minusHours(1)),
                        "이전 날짜로 예약 할 수 없습니다."
                )
        );
    }

    public static Reservation createDefaultReservationWithName(String name) {
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = ThemeFixture.createThemeWithId();
        ReservationTime time = ReservationTimeFixture.createDefault();
        Reservation reservation = Reservation.createSlot(date, theme, time);
        reservation.reserve(name, FIXED);
        return reservation;
    }

    public static Reservation createWithNameAndDate(String name, LocalDate date) {
        Theme theme = ThemeFixture.createThemeWithId();
        ReservationTime time = ReservationTimeFixture.createDefault();
        Reservation reservation = Reservation.createSlot(date, theme, time);
        reservation.reserve(name, FIXED);
        return reservation;
    }

    public static Reservation createWithAll(String name, LocalDate date, Theme theme, ReservationTime time) {
        Reservation reservation = Reservation.createSlot(date, theme, time);
        reservation.reserve(name, FIXED);
        return reservation;
    }
}
