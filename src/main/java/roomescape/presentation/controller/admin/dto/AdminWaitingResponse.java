package roomescape.presentation.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.application.dto.WaitingServiceResponse;

public record AdminWaitingResponse(
        long id,
        String memberName,
        String themeName,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static AdminWaitingResponse from(WaitingServiceResponse waitingResponse) {
        return new AdminWaitingResponse(
                waitingResponse.id(),
                waitingResponse.member().name(),
                waitingResponse.theme().name(),
                waitingResponse.date(),
                waitingResponse.time().startAt()
        );
    }

    public static List<AdminWaitingResponse> from(List<WaitingServiceResponse> waitingResponses) {
        return waitingResponses.stream()
                .map(AdminWaitingResponse::from)
                .toList();
    }
}
