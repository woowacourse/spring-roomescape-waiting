package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.request.ReservationRequestDto;

@Service
@Transactional
public class ReservationService {
    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final ReservationAuthorizationService authorizationService;
    private final WaitingService waitingService;

    public ReservationService(
            ReservationDao reservationDao,
            TimeDao timeDao,
            ThemeDao themeDao,
            ReservationAuthorizationService authorizationService,
            WaitingService waitingService
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.authorizationService = authorizationService;
        this.waitingService = waitingService;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationDao.findAllByMemberId(memberId);
    }

    public Reservation create(Member member, ReservationRequestDto request) {
        Reservation reservation = buildReservation(member, request, LocalDateTime.now());
        try {
            return reservationDao.insert(reservation);
        } catch (DuplicateKeyException e) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
    }

    public Reservation updateByUser(Long id, Long memberId, ReservationPatchDto request) {
        authorizationService.validateMemberCanAccess(memberId, id);
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = findActiveById(id);
        Slot previousSlot = reservation.getSlot();
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time);
        Reservation updated = reservationDao.update(reservation);
        if (updated.hasDifferentSlot(previousSlot)) {
            waitingService.promoteFirstWaiting(previousSlot, now);
        }
        return updated;
    }

    public void cancel(Long id, Long memberId) {
        authorizationService.validateMemberCanAccess(memberId, id);
        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = findActiveById(id);
        reservation.cancelByUser(now);
        reservationDao.update(reservation);
        waitingService.promoteFirstWaiting(reservation.getSlot(), now);
    }

    private Reservation findActiveById(Long id) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        if (!reservation.isActive()) {
            throw new EntityNotFoundException("존재하지 않는 예약입니다.");
        }
        return reservation;
    }

    private Reservation buildReservation(Member member, ReservationRequestDto request, LocalDateTime now) {
        if (reservationDao.existsByThemeIdAndTimeIdAndDateAndStoreIdForUpdate(request.themeId(), request.timeId(),
                request.date(), request.storeId())) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeDao.findById(request.themeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
        return Reservation.createByUser(member, request.date(), time, theme, request.storeId(), now);
    }
}
