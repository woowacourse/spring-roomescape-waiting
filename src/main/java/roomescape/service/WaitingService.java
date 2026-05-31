package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.dto.request.WaitingRequestDto;

@Service
@Transactional
public class WaitingService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
    }

    public Waiting create(WaitingRequestDto waitingRequestDto, Member member) {
        Reservation reservation = reservationDao.findBySlotKeyForUpdate(waitingRequestDto.themeId(),
                        waitingRequestDto.timeId(), waitingRequestDto.date(), waitingRequestDto.storeId())
                .orElseThrow(() -> new BusinessRuleViolationException("예약이 존재하지 않아 대기가 불가능합니다."));

        Waitings waitings = waitingDao.findQueueBySlotForUpdate(reservation.getSlot());
        Waiting waiting = waitings.create(member, reservation);
        Waiting saved = waitingDao.insert(waiting);
        return saved.withRank(waitings.nextRank());
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAll() {
        return flattenRanked(waitingDao.findAllQueues());
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByMemberId(Long memberId) {
        return waitingDao.findQueuesContainingMember(memberId).stream()
                .flatMap(waitings -> waitings.assignRanksOfMember(memberId).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByStoreId(Long storeId) {
        return flattenRanked(waitingDao.findQueuesByStoreId(storeId));
    }

    private List<Waiting> flattenRanked(List<Waitings> queues) {
        return queues.stream()
                .flatMap(waitings -> waitings.assignRanks().stream())
                .toList();
    }
}
