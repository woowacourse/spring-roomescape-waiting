package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.ReservationStatus;
import roomescape.repository.dto.WaitDetailDto;

public record ServiceWaitResponse(
        Long id,
        String name,
        LocalDate date,
        ServiceReservationTimeResponse time,
        ServiceThemeResponse theme,
        ReservationStatus status,
        Long order,
        LocalDateTime createdAt
) implements ServiceReceptionResponse {

    public static ServiceWaitResponse from(WaitDetailDto wait) {
        return new ServiceWaitResponse(
                wait.id(),
                wait.name(),
                wait.reservationDate(),
                ServiceReservationTimeResponse.from(wait.reservationTime()),
                ServiceThemeResponse.from(wait.theme()),
                ReservationStatus.WAITING,
                wait.order(),
                wait.createdAt()
        );
    }
}
