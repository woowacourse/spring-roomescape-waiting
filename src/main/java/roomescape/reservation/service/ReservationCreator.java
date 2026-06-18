package roomescape.reservation.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.vo.Slot;
import roomescape.member.Member;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.web.AdminReservationRequestDto;
import roomescape.reservation.web.ReservationRequestDto;
import roomescape.store.Store;
import roomescape.store.StoreDao;
import roomescape.theme.Theme;
import roomescape.theme.ThemeDao;
import roomescape.time.Time;
import roomescape.time.TimeDao;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingDao;

@Component
public class ReservationCreator {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final StoreDao storeDao;
    private final WaitingDao waitingDao;

    public ReservationCreator(
            ReservationDao reservationDao,
            TimeDao timeDao,
            ThemeDao themeDao,
            StoreDao storeDao,
            WaitingDao waitingDao
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.storeDao = storeDao;
        this.waitingDao = waitingDao;
    }

    public Reservation createByUser(Member member, ReservationRequestDto request, LocalDateTime now) {
        Time time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Store store = findStore(request.storeId());
        Slot slot = new Slot(request.date(), time, theme, store);
        validateAvailable(slot);
        validateNoWaitingQueue(slot);

        return Reservation.createByUser(member, request.date(), time, theme, store, now);
    }

    public Reservation createByAdmin(Member member, AdminReservationRequestDto request) {
        Time time = findTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Store store = findStore(request.storeId());
        Slot slot = new Slot(request.date(), time, theme, store);
        validateAvailable(slot);

        return Reservation.createByAdmin(member, request.date(), time, theme, store);
    }

    /**
     * 승격 전용 생성: 대기를 PENDING 예약으로 만들어 저장한다. 유저 생성(createByUser)과 달리
     * 권한·새치기(대기 큐) 검증을 거치지 않는다 — 승격은 시스템이 수행하는 완전히 다른 행동이다.
     */
    public Reservation createFromPromotion(Waiting waiting, LocalDateTime now) {
        return reservationDao.insert(waiting.promote(now));
    }

    private void validateAvailable(Slot slot) {
        if (reservationDao.existsBySlotForUpdate(slot)) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
    }

    /**
     * 새치기 방지: 취소 직후 슬롯이 비어 보이는 순간(아웃박스 승격 대기 중)에 다른 사용자가 대기자를 제치고 직접 예약하는 것을 막는다. 대기 행을 잠금 조회하여 워커의 승격과 직렬화한다.
     */
    private void validateNoWaitingQueue(Slot slot) {
        if (!waitingDao.findQueueBySlotForUpdate(slot).isEmpty()) {
            throw new BusinessRuleViolationException("대기자가 있는 슬롯입니다. 대기 신청을 이용해주세요.");
        }
    }

    private Time findTime(Long id) {
        return timeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
    }

    private Theme findTheme(Long id) {
        return themeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
    }

    private Store findStore(Long id) {
        return storeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 매장입니다."));
    }
}
