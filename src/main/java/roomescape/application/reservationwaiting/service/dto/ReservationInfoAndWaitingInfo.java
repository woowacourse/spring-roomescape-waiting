package roomescape.application.reservationwaiting.service.dto;

import java.util.List;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.service.dto.WaitingInfo;

public record ReservationInfoAndWaitingInfo(List<ReservationInfo> reservationInfos, List<WaitingInfo> waitingInfos) {
}
