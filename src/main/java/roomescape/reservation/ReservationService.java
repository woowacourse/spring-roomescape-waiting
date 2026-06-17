package roomescape.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.vo.Slot;
import roomescape.time.TimeDao;
import roomescape.member.Member;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.promotion.PromotionService;
import roomescape.time.Time;
import roomescape.reservation.web.ReservationPatchDto;
import roomescape.reservation.web.ReservationRequestDto;

@Service
@Transactional
public class ReservationService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final PromotionService promotionService;
    private final ReservationAuthorizationService authorizationService;
    private final ReservationCreator reservationCreator;
    private final OrderService orderService;

    public ReservationService(
            ReservationDao reservationDao,
            TimeDao timeDao,
            PromotionService promotionService,
            ReservationAuthorizationService authorizationService,
            ReservationCreator reservationCreator,
            OrderService orderService
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.promotionService = promotionService;
        this.authorizationService = authorizationService;
        this.reservationCreator = reservationCreator;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationDao.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public Reservation findActiveById(Long id) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        if (!reservation.isActive()) {
            throw new EntityNotFoundException("존재하지 않는 예약입니다.");
        }
        return reservation;
    }

    public ReservationOrder create(Member member, ReservationRequestDto request) {
        Reservation reservation = reservationCreator.createByUser(member, request, LocalDateTime.now());
        Reservation saved;
        try {
            saved = reservationDao.insert(reservation);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
        Order order = orderService.create(saved.getId(), saved.getTheme().getPrice());
        return new ReservationOrder(saved, order);
    }

    public Reservation updateByUser(Long id, Member member, ReservationPatchDto request) {
        authorizationService.validateMemberCanAccess(member, id);
        Reservation reservation = findActiveById(id);
        Slot vacatedSlot = reservation.getSlot();
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time, LocalDateTime.now());
        Reservation updated = reservationDao.update(reservation);
        promotionService.enqueuePromotion(vacatedSlot);
        return updated;
    }

    public void cancel(Long id, Member member) {
        authorizationService.validateMemberCanAccess(member, id);
        Reservation reservation = findActiveById(id);
        reservation.cancelByUser(LocalDateTime.now());
        reservationDao.update(reservation);
        promotionService.enqueuePromotion(reservation.getSlot());
    }

    /**
     * 결제 승인 성공 → 결제 대기(PENDING) 예약을 확정(BOOKED)으로. 결제 흐름이 위임한다.
     */
    public void confirm(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        reservation.confirm(LocalDateTime.now());
        reservationDao.update(reservation);
    }

    /**
     * 결제 실패·만료 등으로 PENDING 예약을 취소(슬롯 해제)하고 다음 대기자 승격을 예약한다.
     * 이미 정리됐으면 조용히 건너뛴다(멱등).
     */
    public void cancelPending(Long reservationId) {
        reservationDao.findById(reservationId).ifPresent(reservation -> {
            reservation.cancelPending(LocalDateTime.now());
            reservationDao.update(reservation);
            promotionService.enqueuePromotion(reservation.getSlot());
        });
    }

    /**
     * 주문조차 없는 방치 PENDING(무료 승격 대기 등) 후보. 주문 기준 reaper의 사각지대 — 예약 created_at 기준.
     */
    @Transactional(readOnly = true)
    public List<Long> findExpiredOrphanPendingIds(LocalDateTime threshold) {
        return reservationDao.findExpiredPendingIdsWithoutOrder(threshold);
    }
}
