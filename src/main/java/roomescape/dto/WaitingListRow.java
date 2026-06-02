package roomescape.dto;

import roomescape.domain.WaitingList;

public record WaitingListRow(
        WaitingList waitingList,
        int waitingOrder
) {
}
