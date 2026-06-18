package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.PageResponse;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.vo.Slot;
import roomescape.member.Member;
import roomescape.member.MemberDao;
import roomescape.promotion.PromotionService;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.web.dto.AdminReservationRequestDto;
import roomescape.reservation.web.dto.ReservationPatchDto;
import roomescape.time.Time;
import roomescape.time.TimeDao;

@Service
@Transactional
public class AdminReservationService {
    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final TimeDao timeDao;
    private final PromotionService promotionService;
    private final ReservationCreator reservationCreator;

    public AdminReservationService(
            ReservationDao reservationDao,
            MemberDao memberDao,
            TimeDao timeDao,
            PromotionService promotionService,
            ReservationCreator reservationCreator
    ) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.timeDao = timeDao;
        this.promotionService = promotionService;
        this.reservationCreator = reservationCreator;
    }

    @Transactional(readOnly = true)
    public PageResponse<Reservation> findAll(int page, int size) {
        int offset = page * size;
        List<Reservation> content = reservationDao.findAll(size, offset);
        long totalElements = reservationDao.count();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, totalElements, totalPages, page, size);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByStoreId(Long storeId) {
        return reservationDao.findAllByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }

    public Reservation createByAdmin(AdminReservationRequestDto request) {
        Member member = memberDao.findById(request.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버입니다."));
        Reservation reservation = reservationCreator.createByAdmin(member, request);
        return reservationDao.insert(reservation);
    }

    public Reservation update(Long id, ReservationPatchDto request) {
        Reservation reservation = findById(id);
        Slot vacatedSlot = reservation.getSlot();
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time, LocalDateTime.now());
        Reservation updated = reservationDao.update(reservation);
        promotionService.enqueuePromotion(vacatedSlot);
        return updated;
    }

    public void cancelByAdmin(Long id) {
        Reservation reservation = findById(id);
        reservation.cancelByAdmin(LocalDateTime.now());
        reservationDao.update(reservation);
        promotionService.enqueuePromotion(reservation.getSlot());
    }

    public void delete(Long id) {
        if (!reservationDao.delete(id)) {
            throw new EntityNotFoundException("존재하지 않는 예약입니다.");
        }
    }
}
