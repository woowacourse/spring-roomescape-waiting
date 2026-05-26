package roomescape.service;

import java.util.List;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.WaitingDao;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequestDto;

public class WaitingService {
    private final WaitingDao waitingDao;

    public WaitingService(WaitingDao waitingDao) {
        this.waitingDao = waitingDao;
    }

    public Waiting create(WaitingRequestDto waitingRequestDto) {
        return waitingDao.insert(new Waiting(
                waitingRequestDto.memberId(),
                waitingRequestDto.date(),
                waitingRequestDto.timeId(),
                waitingRequestDto.themeId(),
                waitingRequestDto.storeId()
        ));
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    public List<Waiting> findAll(){
        return waitingDao.findAll();
    }

    public List<Waiting> findAllByMemberId(Long memberId){
        return waitingDao.findAllByMemberId(memberId);
    }
}
