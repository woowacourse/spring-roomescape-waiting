package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.PromotionOutboxDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Member;
import roomescape.domain.PromotionTask;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.dto.request.WaitingRequestDto;

@Service
@Transactional
public class WaitingService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final PromotionOutboxDao promotionOutboxDao;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao,
                          PromotionOutboxDao promotionOutboxDao) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
        this.promotionOutboxDao = promotionOutboxDao;
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

    /**
     * 취소 트랜잭션 안에서 호출된다. inline으로 승격하지 않고, "이 슬롯의 다음 대기자를 승격시켜라"라는
     * 할 일만 아웃박스에 기록한다(취소와 같은 트랜잭션 → 원자적). 실제 승격은 워커가 나중에 수행한다.
     */
    public void enqueuePromotion(Slot slot) {
        promotionOutboxDao.insert(PromotionTask.pending(slot));
    }

    /**
     * 워커가 아웃박스 할 일을 처리할 때 호출된다. 여러 번 실행되어도 결과가 같도록 멱등하게 설계했다:
     * 이미 해당 슬롯에 활성 예약이 있으면(=이미 승격됨 또는 그새 누가 예약함) 아무것도 하지 않는다.
     */
    public void promotePendingSlot(Long themeId, Long timeId, LocalDate date, Long storeId) {
        if (reservationDao.findBySlotKeyForUpdate(themeId, timeId, date, storeId).isPresent()) {
            return;
        }
        waitingDao.findFirstBySlotKeyForUpdate(themeId, timeId, date, storeId).ifPresent(first -> {
            if (first.getSlot().isPast(LocalDateTime.now())) {
                return;
            }
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
