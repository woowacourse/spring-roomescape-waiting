package roomescape.repository;

import roomescape.domain.WaitingList;

public record WaitingListRow(
        WaitingList waitingList,
        int waitingOrder
) {
}
