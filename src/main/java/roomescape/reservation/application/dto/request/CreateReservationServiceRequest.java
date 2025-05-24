package roomescape.reservation.application.dto.request;

import java.time.LocalDate;
import roomescape.reservation.model.vo.Schedule;

public record  CreateReservationServiceRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public Schedule toSchedule() {
        return Schedule.builder()
                .date(date)
                .timeId(timeId)
                .themeId(themeId)
                .build();
    }
}
