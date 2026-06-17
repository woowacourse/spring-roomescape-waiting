package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;

public class ReservationFixture {

    private static final String STARK = "스타크";
    private static final String KAYA = "카야";
    private static final LocalDate PAST_RESERVATION_DATE = LocalDate.of(2000, 5, 6);
    private static final LocalDate FUTURE_RESERVATION_DATE = LocalDate.of(2099, 12, 31);
    private static final String PAST_RESERVATION_DATE_TEXT = "2000-05-06";
    private static final String FUTURE_RESERVATION_DATE_TEXT = "2099-12-31";

    private ReservationFixture() {
    }

    public static ReservationApplicationCreateCommand pastStarkCreateCommand(Long themeId, Long timeId,
                                                                             LocalDateTime now) {
        return new ReservationApplicationCreateCommand(STARK, PAST_RESERVATION_DATE, themeId, timeId, "mock_payment_key", 1000L, now);
    }

    public static ReservationApplicationCreateCommand futureStarkCreateCommand(Long themeId, Long timeId,
                                                                               LocalDateTime now) {
        return new ReservationApplicationCreateCommand(STARK, FUTURE_RESERVATION_DATE, themeId, timeId, "mock_payment_key", 1000L, now);
    }

    public static ReservationApplicationCreateCommand futureKayaCreateCommand(Long themeId, Long timeId,
                                                                              LocalDateTime now) {
        return new ReservationApplicationCreateCommand(KAYA, FUTURE_RESERVATION_DATE, themeId, timeId, "mock_payment_key", 1000L, now);
    }

    public static ReservationUpdateCommand futureStarkUpdateCommand(Long timeId, LocalDateTime now) {
        return new ReservationUpdateCommand(FUTURE_RESERVATION_DATE, timeId, now);
    }

    public static LocalDate pastReservationDate() {
        return PAST_RESERVATION_DATE;
    }

    public static LocalDate futureReservationDate() {
        return FUTURE_RESERVATION_DATE;
    }

    public static Map<String, String> futureReservationParams(Long themeId, Long timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("name", STARK);
        params.put("date", FUTURE_RESERVATION_DATE_TEXT);
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));
        return params;
    }

    public static Map<String, String> pastReservationParams(Long themeId, Long timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("name", STARK);
        params.put("date", PAST_RESERVATION_DATE_TEXT);
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));
        return params;
    }

    public static Map<String, String> futureReservationUpdateParams(Long timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("date", FUTURE_RESERVATION_DATE_TEXT);
        params.put("timeId", String.valueOf(timeId));
        return params;
    }

    public static Map<String, String> pastReservationUpdateParams(Long timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("date", PAST_RESERVATION_DATE_TEXT);
        params.put("timeId", String.valueOf(timeId));
        return params;
    }
}
