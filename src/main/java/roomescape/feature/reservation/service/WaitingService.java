package roomescape.feature.reservation.service;

import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;

public interface WaitingService {

    ReservationCreateResponseDto saveWaitingReservation(ReservationCreateCommand command);

    ReservationCancelResponseDto cancelWaitingReservation(Long id, ReserverName name);
}
