package roomescape.reservation.domain.fixture;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.fixture.ReservationTimeFixture;

public final class ReservationFixture {

    public static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-01T09:00:00Z"), ZoneId.of("Asia/Seoul"));

    private ReservationFixture() {
    }

    public static Stream<Arguments> invalidReservationConstructor() {
        return Stream.of(
                Arguments.of(null, LocalDate.now(FIXED_CLOCK).plusDays(1), ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefaultReservationTime(), "예약자 이름은 필수입니다."),
                Arguments.of("바니", null, ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefaultReservationTime(), "날짜는 필수입니다."),
                Arguments.of("바니", LocalDate.now(FIXED_CLOCK).plusDays(1), null,
                        ReservationTimeFixture.createDefaultReservationTime(), "테마는 필수입니다."),
                Arguments.of("바니", LocalDate.now(FIXED_CLOCK).plusDays(1), ThemeFixture.createDefaultTheme(),
                        null, "시간은 필수입니다.")
        );
    }

    public static Stream<Arguments> pastReservationDateTimeConstructor() {
        return Stream.of(
                Arguments.of(LocalDate.now(FIXED_CLOCK).minusDays(1), ThemeFixture.createDefaultTheme(),
                        ReservationTimeFixture.createDefaultReservationTime(), "현재보다 이전 시간대로 예약할 수 없습니다."),
                Arguments.of(LocalDate.now(FIXED_CLOCK), ThemeFixture.createDefaultTheme(),
                        ReservationTime.create(LocalTime.MIN), "현재보다 이전 시간대로 예약할 수 없습니다.")
        );
    }

    public static Reservation createDefaultReservation() {
        return createDefaultReservation("바니");
    }

    public static Reservation createDefaultReservation(String name) {
        return createDefaultReservation(name, ThemeFixture.createThemeWithId(),
                ReservationTimeFixture.createDefaultReservationTime());
    }

    public static Reservation createDefaultReservation(String name, Theme theme, ReservationTime time) {
        return Reservation.create(name, LocalDate.now(FIXED_CLOCK).plusDays(1), time, theme, Status.RESERVED, FIXED_CLOCK);
    }

    public static Reservation createWaitingReservation(String name, Theme theme, ReservationTime time) {
        return Reservation.create(name, LocalDate.now(FIXED_CLOCK).plusDays(1), time, theme, Status.WAITING, FIXED_CLOCK);
    }

    public static Reservation restoreDefaultReservation(Long id, String name, Theme theme, ReservationTime time,
                                                        Status status) {
        return Reservation.restore(id, name, LocalDate.now(FIXED_CLOCK).plusDays(1), time, theme, status,
                LocalDateTime.now(FIXED_CLOCK));
    }
}
