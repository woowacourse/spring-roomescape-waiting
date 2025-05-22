package roomescape.presentation.dto.response;

import roomescape.business.dto.MyReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MyReservationResponse(
        String id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(MyReservationDto myReservations) {

        return new MyReservationResponse(
                myReservations.id().value(),
                myReservations.theme().name().value(),
                myReservations.date().value(),
                myReservations.time().startTime().value(),
                parseStatus(myReservations.waitNumber())
        );
    }

    private static String parseStatus(final int waitNumber) {
        if (waitNumber == 0) {
            return "예약";
        }
        return waitNumber + "번째 대기";
    }

    public static List<MyReservationResponse> from(List<MyReservationDto> myReservations) {
        return myReservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
