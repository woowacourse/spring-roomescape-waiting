package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.TimeDao;
import roomescape.domain.member.Member;
import roomescape.domain.vo.Slot;
import roomescape.domain.time.Time;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.domain.promotion.PromotionService;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.PaymentService;

@Service
@Transactional
public class ReservationService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final PromotionService promotionService;
    private final ReservationAuthorizationService authorizationService;
    private final ReservationCreator reservationCreator;
    private final PaymentService paymentService;

    public ReservationService(
            ReservationDao reservationDao,
            TimeDao timeDao,
            PromotionService promotionService,
            ReservationAuthorizationService authorizationService,
            ReservationCreator reservationCreator,
            PaymentService paymentService
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.promotionService = promotionService;
        this.authorizationService = authorizationService;
        this.reservationCreator = reservationCreator;
        this.paymentService = paymentService;
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
        Order order = paymentService.createOrder(saved.getId(), saved.getTheme().getPrice());
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
}
