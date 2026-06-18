package roomescape.controller.client.api.query;

import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;

public interface ReservationQuery {

    ReservationDetailResponse findByReservationId(long reservationId);

    Page<ReservationSearchResponse> search(ReservationSearchCondition condition, Pageable pageable);
}
