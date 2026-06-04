package roomescape.domain.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
