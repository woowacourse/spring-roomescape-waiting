package roomescape.fixture;

import java.time.LocalDate;
import roomescape.reservation.web.dto.ReservationRequestDto;

public class ReservationRequestDtoFixture {

    private static final LocalDate VALID_DATE = LocalDate.now().plusDays(1);
    private static final Long VALID_TIME_ID = 1L;
    private static final Long VALID_THEME_ID = 1L;

    public static ReservationRequestDto withNullDate() {
        return new ReservationRequestDto(null, VALID_TIME_ID, VALID_THEME_ID, null);
    }

    public static ReservationRequestDto withNullTimeId() {
        return new ReservationRequestDto(VALID_DATE, null, VALID_THEME_ID, null);
    }

    public static ReservationRequestDto withNullThemeId() {
        return new ReservationRequestDto(VALID_DATE, VALID_TIME_ID, null, null);
    }
}
