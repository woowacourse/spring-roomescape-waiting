package roomescape.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dao.WaitingDao;
import roomescape.domain.Waiting;

@Service
public class WaitingAuthorizationService {
    private final WaitingDao waitingDao;

    public WaitingAuthorizationService(WaitingDao waitingDao) {
        this.waitingDao = waitingDao;
    }

    public void validateMemberCanAccess(Long memberId, Long waitingId
    ) {
        Waiting waiting = findWaiting(waitingId);
        if (!waiting.getMember().getId().equals(memberId)) {
            throw new HiddenResourceException();
        }
    }

    public void validateManagerCanAccess(Long storeId, Long waitingId
    ) {
        if (storeId == null) {
            throw new UnauthorizedException();
        }
        Waiting waiting = findWaiting(waitingId);
        if (!Objects.equals(storeId, waiting.getStoreId())) {
            throw new UnauthorizedException();
        }
    }

    private Waiting findWaiting(Long waitingId
    ) {
        return waitingDao.findById(waitingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 대기입니다."));
    }
}
