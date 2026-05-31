package roomescape.controller.admin.api.query;

import java.util.List;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;

public interface AdminReservationQuery {

    List<AdminReservationSlotResponse> getAllReservations();
}
