package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.waiting.WaitingDao;
import roomescape.member.Member;
import roomescape.store.Store;
import roomescape.waiting.Waiting;

@Service
public class WaitingAuthorizationService {
    private final WaitingDao waitingDao;

    public WaitingAuthorizationService(WaitingDao waitingDao) {
        this.waitingDao = waitingDao;
    }

    public void validateMemberCanAccess(Member member, Long waitingId) {
        Waiting waiting = findWaiting(waitingId);
        if (!waiting.isSameMember(member)) {
            throw new HiddenResourceException();
        }
    }

    public void validateManagerCanAccess(Member manager, Long waitingId) {
        Store store = manager.getStore();
        if (store == null) {
            throw new UnauthorizedException();
        }
        Waiting waiting = findWaiting(waitingId);
        if (!waiting.isInStore(store)) {
            throw new UnauthorizedException();
        }
    }

    private Waiting findWaiting(Long waitingId) {
        return waitingDao.findById(waitingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 대기입니다."));
    }
}
