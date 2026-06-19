package roomescape.feature.reservation.service;

import java.util.List;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;

public interface ReservationService {

    List<ReservationResponseDto> getReservationsByName(ReserverName name);

    ReservationCreateResponseDto saveReservation(ReservationCreateCommand command);

    ReservationCreateResponseDto updateReservation(Long id, ReservationUpdateCommand command);

    void confirmReservation(Long reservationId, PaymentApproveRequest request);

    ReservationCancelResponseDto cancelReservation(Long id, ReserverName name);
}
