package roomescape.domain.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class ReservationFixture {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2000, 1, 2);
    private static final LocalTime REFERENCE_TIME = LocalTime.of(10, 0);

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
                        REFERENCE_DATE,
                        null,
                        ReservationTimeFixture.createDefault(),
                        "테마 정보는 비어있을 수 없습니다."
                ),
                // 예약 시간 정보가 누락된 경우
                Arguments.of(
                        REFERENCE_DATE,
                        ThemeFixture.createDefaultTheme(),
                        null,
                        "예약 날짜 및 시간 정보는 비어있을 수 없습니다."
                ),
                // 오늘보다 과거 날짜인 경우
                Arguments.of(
                        REFERENCE_DATE.minusDays(1),
                        ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefault(),
                        "이전 날짜로 예약 할 수 없습니다."
                ),
                // 기준 일시보다 과거인 경우
                Arguments.of(
                        REFERENCE_DATE,
                        ThemeFixture.createDefaultTheme(),
                        new ReservationTime(REFERENCE_TIME.minusHours(1)),
                        "이전 날짜로 예약 할 수 없습니다."
                )
        );
    }

    public static ReservationSlot createDefaultReservationWithName(String name) {
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = ThemeFixture.createThemeWithId();
        ReservationTime time = ReservationTimeFixture.createDefault();
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, time);
        slot.reserve(name);
        return slot;
    }

    public static ReservationSlot createWithNameAndDate(String name, LocalDate date) {
        Theme theme = ThemeFixture.createThemeWithId();
        ReservationTime time = ReservationTimeFixture.createDefault();
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, time);
        slot.reserve(name);
        return slot;
    }

    public static ReservationSlot createWithAll(String name, LocalDate date, Theme theme, ReservationTime time) {
        ReservationSlot slot = ReservationSlot.createSlot(date, theme, time);
        slot.reserve(name);
        return slot;
    }
}
