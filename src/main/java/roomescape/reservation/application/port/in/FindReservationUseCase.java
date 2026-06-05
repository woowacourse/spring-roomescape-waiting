package roomescape.reservation.application.port.in;

import java.util.List;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;

public interface FindReservationUseCase {
    List<ReservationDetailFindResponse> findReservationDetails();
    List<ReservationDetailFindResponse> findMyReservations(long memberId);
}
