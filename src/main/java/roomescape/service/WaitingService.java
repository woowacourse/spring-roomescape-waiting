package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
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
        Waiting ranked = waitings.enqueue(member, reservation, LocalDateTime.now());
        try {
            return waitingDao.insert(ranked);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 대기 신청한 슬롯입니다.");
        }
    }

    public void delete(Long waitingId) {
        if (!waitingDao.delete(waitingId)) {
            throw new EntityNotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }

    public void promoteFirstWaiting(Reservation canceled) {
        if (canceled.getSlot().isPast(LocalDateTime.now())) {
            return;
        }
        Waitings waitings = waitingDao.findQueueBySlotForUpdate(canceled.getSlot());
        waitings.peekFirst().ifPresent(first -> {
            reservationDao.insert(first.promote());
            waitingDao.delete(first.getId());
        });
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAll() {
        return waitingDao.findAllQueues().stream()
                .flatMap(queue -> queue.getAll().stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByMemberId(Long memberId) {
        return waitingDao.findQueuesContainingMember(memberId).stream()
                .flatMap(queue -> queue.ofMember(memberId).stream())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByStoreId(Long storeId) {
        return waitingDao.findQueuesByStoreId(storeId).stream()
                .flatMap(queue -> queue.getAll().stream())
                .toList();
    }
}
