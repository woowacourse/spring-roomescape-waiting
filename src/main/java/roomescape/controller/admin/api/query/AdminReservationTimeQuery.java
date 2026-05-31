package roomescape.controller.admin.api.query;

import java.util.List;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;

public interface AdminReservationTimeQuery {

    List<AdminReservationTimeResponse> getAllReservationTimes();
}
