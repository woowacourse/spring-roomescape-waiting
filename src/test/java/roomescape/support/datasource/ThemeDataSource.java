package roomescape.support.datasource;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class ThemeDataSource extends BaseDataSource {

    public void insertThemesByCount(int count) {
        for (int i = 0; i < count; i++) {
            insertTheme("테마" + i, "https://image.com/image" + i + ".png", "설명" + i);
        }
    }

    public void insertTimeByStartToEndWithOneHourRotation(int startHour, int endHour) {
        for (int i = startHour; i <= endHour; i++) {
            insertReservationTime(LocalTime.of(i, 0));
        }
    }

    public void insertReservedReservationByTheme(Long themeId, int reservationCount) {
        for (long timeId = 1L; timeId <= reservationCount; timeId++) {
            insertReservation("바니" + timeId, LocalDate.now(), themeId, timeId, "RESERVED");
        }
    }
}
