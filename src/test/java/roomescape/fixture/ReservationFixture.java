package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.dto.WaitingCreateCommand;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;

public class ReservationFixture {

    private static final String STARK = "스타크";
    private static final String KAYA = "카야";
    private static final String PINO = "피노";
    private static final String NEO = "네오";
    private static final LocalDate PAST_RESERVATION_DATE = LocalDate.of(2000, 5, 6);
    private static final LocalDate FUTURE_RESERVATION_DATE = LocalDate.of(2099, 12, 31);
    private static final LocalDate FUTURE_RESERVATION_UPDATE_DATE = LocalDate.of(2100, 1, 1);
    private static final String PAST_RESERVATION_DATE_TEXT = "2000-05-06";
    private static final String FUTURE_RESERVATION_DATE_TEXT = "2099-12-31";
    private static final String FUTURE_RESERVATION_UPDATE_DATE_TEXT = "2100-01-01";

    private ReservationFixture() {
    }

    public static ReservationCreateCommand pastStarkCreateCommand(Long themeId, Long timeId,
                                                                  LocalDateTime now) {
        return new ReservationCreateCommand(STARK, PAST_RESERVATION_DATE, themeId, timeId, now);
    }

    public static ReservationCreateCommand futureStarkCreateCommand(Long themeId, Long timeId,
                                                                    LocalDateTime now) {
        return new ReservationCreateCommand(STARK, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static ReservationCreateCommand futureKayaCreateCommand(Long themeId, Long timeId,
                                                                   LocalDateTime now) {
        return new ReservationCreateCommand(KAYA, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static WaitingCreateCommand futureStarkWaitingCreateCommand(Long themeId, Long timeId,
                                                                       LocalDateTime now) {
        return new WaitingCreateCommand(STARK, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static WaitingCreateCommand futureKayaWaitingCreateCommand(Long themeId, Long timeId,
                                                                      LocalDateTime now) {
        return new WaitingCreateCommand(KAYA, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static WaitingCreateCommand futurePinoWaitingCreateCommand(Long themeId, Long timeId,
                                                                      LocalDateTime now) {
        return new WaitingCreateCommand(PINO, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static WaitingCreateCommand futureNeoWaitingCreateCommand(Long themeId, Long timeId,
                                                                     LocalDateTime now) {
        return new WaitingCreateCommand(NEO, FUTURE_RESERVATION_DATE, themeId, timeId, now);
    }

    public static ReservationUpdateCommand futureStarkUpdateCommand(Long timeId, LocalDateTime now) {
        return new ReservationUpdateCommand(FUTURE_RESERVATION_UPDATE_DATE, timeId, now);
    }

    public static LocalDate pastReservationDate() {
        return PAST_RESERVATION_DATE;
    }

    public static LocalDate futureReservationDate() {
        return FUTURE_RESERVATION_DATE;
    }

    public static LocalDate futureReservationUpdateDate() {
        return FUTURE_RESERVATION_UPDATE_DATE;
    }

    public static User userNameStark() {
        return User.builder()
                .name(STARK)
                .build();
    }

    public static User userNamePino() {
        return User.builder()
                .name(PINO)
                .build();
    }

    public static User userNameNeo() {
        return User.builder()
                .name(NEO)
                .build();
    }

    public static ReservationSlot futureReservationSlot(Long themeId, Long timeId, LocalTime startAt) {
        return ReservationSlot.builder()
                .date(FUTURE_RESERVATION_DATE)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }

    public static Map<String, String> futureReservationParams(Long themeId, Long timeId) {
        Map<String, String> params = new HashMap<>();
        params.put("name", STARK);
        params.put("date", FUTURE_RESERVATION_DATE_TEXT);
        params.put("themeId", String.valueOf(themeId));
        params.put("timeId", String.valueOf(timeId));
        return params;
    }

    public static Map<String, String> futureWaitingParams(Long themeId, Long timeId) {
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
        params.put("date", FUTURE_RESERVATION_UPDATE_DATE_TEXT);
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
