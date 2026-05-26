package roomescape.repository;

import roomescape.service.dto.WaitingCommand;

public interface WaitingRepository {

    int calculateWaitingNumberByName(WaitingCommand waiting);

    void insert(WaitingCommand waiting);

    void delete(WaitingCommand waiting);
}
