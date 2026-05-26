package roomescape.repository;

import roomescape.service.dto.WaitingCommand;

public interface WaitingRepository {

    int calculateWaitingNumber(WaitingCommand waiting);

    void save(WaitingCommand waiting);

    void delete(WaitingCommand waiting);
}
