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
import roomescape.dao.StoreDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Store;
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
    private final StoreDao storeDao;
    private final ReservationAuthorizationService authorizationService;

    public ReservationService(
            ReservationDao reservationDao,
            TimeDao timeDao,
            ThemeDao themeDao,
            StoreDao storeDao,
            ReservationAuthorizationService authorizationService
    ) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.storeDao = storeDao;
        this.authorizationService = authorizationService;
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
        Reservation reservation = findActiveById(id);
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time, LocalDateTime.now());
        return reservationDao.update(reservation);
    }

    public void cancel(Long id, Long memberId) {
        authorizationService.validateMemberCanAccess(memberId, id);
        Reservation reservation = findActiveById(id);
        reservation.cancelByUser(LocalDateTime.now());
        reservationDao.update(reservation);
    }

    private Reservation buildReservation(Member member, ReservationRequestDto request, LocalDateTime now) {
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeDao.findById(request.themeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
        Store store = storeDao.findById(request.storeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 매장입니다."));
        Slot slot = new Slot(request.date(), time, theme, store);
        if (reservationDao.existsBySlotForUpdate(slot)) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
        return Reservation.createByUser(member, request.date(), time, theme, store, now);
    }
}
