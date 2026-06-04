package roomescape.support.datasource;

import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class ReservationTimeDataSource extends BaseDataSource {

    public void insertTimeByStartToEndWithOneHourRotation(int startHour, int endHour) {
        for (int i = startHour; i <= endHour; i++) {
            insertReservationTime(LocalTime.of(i, 0));
        }
    }
}
