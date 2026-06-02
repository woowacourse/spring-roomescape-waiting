package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ReservationWaitingDao;
import roomescape.repository.SlotDao;
import roomescape.repository.ThemeQueryingDao;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final SlotDao slotDao;
    private final ReservationQueryingDao reservationQueryingDao;
    private final ReservationUpdatingDao reservationUpdatingDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;
    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationService(SlotDao slotDao, ReservationQueryingDao reservationQueryingDao,
                              ReservationUpdatingDao reservationUpdatingDao, ReservationTimeQueryingDao reservationTimeQueryingDao,
                              ThemeQueryingDao themeQueryingDao, ReservationWaitingDao reservationWaitingDao) {
        this.slotDao = slotDao;
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public ReservationResponse read(Long id) {
        return ReservationResponse.from(getReservation(id));
    }

    public List<ReservationResponse> readAll() {
        return reservationQueryingDao.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readByName(String name) {
        return reservationQueryingDao.findAllByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse create(ReservationRequest reservationReq) {
        try {
            Slot slot = createSlot(reservationReq);
            Reservation reservation = Reservation.create(reservationReq.name(), slot);
            Long reservationId = reservationUpdatingDao.insert(reservation);
            return ReservationResponse.from(reservation.withId(reservationId));
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existed = getReservation(id);

        if (existed.isSameSlot(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            Reservation updated = existed.update(reservationRequest.name());
            reservationUpdatingDao.updateName(id, reservationRequest.name());
            return ReservationResponse.from(updated);
        }

        try {
            Long previousSlotId = existed.getSlotId();
            Reservation moved = updateSlot(existed, reservationRequest);
            promoteOrCleanupSlot(previousSlotId);
            return ReservationResponse.from(moved);
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    @Transactional
    public void delete(Long id) {
        Optional<Reservation> optionalReservation = reservationQueryingDao.findReservationById(id);
        if (optionalReservation.isEmpty()) {
            throw new ResourceNotFoundException("해당 예약이 존재하지 않습니다.");
        }
        Reservation reservation = optionalReservation.get();
        if (reservation.isExpired()) {
            throw new ExpiredDateTimeException();
        }

        long deleted = reservationUpdatingDao.delete(reservation.getId());
        if (deleted == 0) {
            throw new ResourceNotFoundException("해당 예약이 존재하지 않습니다.");
        }
        promoteOrCleanupSlot(reservation.getSlot().getId());
    }

    private void promoteOrCleanupSlot(Long slotId) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.findFirstBySlotId(slotId);
        if (firstWaiting.isEmpty()) {
            deleteSlot(slotId);
            return;
        }

        promoteWaiting(firstWaiting.get());
    }

    private void promoteWaiting(ReservationWaiting reservationWaiting) {
        long claimed = reservationWaitingDao.delete(reservationWaiting.getId());
        if (claimed == 0) {
            throw new DataIntegrityViolationException("대기열이 변경되었습니다. 다시 시도해주세요.");
        }
        reservationUpdatingDao.insert(reservationWaiting.promote());
    }

    private Reservation updateSlot(Reservation existed, ReservationRequest request) {
        Slot newSlot = createSlot(request);
        Reservation moved = existed.update(request.name(), newSlot);

        long updated = reservationUpdatingDao.update(moved.getId(), moved.getName(), newSlot.getId(), moved.getCreatedAt());
        if (updated == 0) {
            throw new ResourceNotFoundException("해당 예약이 존재하지 않습니다.");
        }
        return moved;
    }

    private Slot createSlot(ReservationRequest request) {
        ReservationTime time = reservationTimeQueryingDao.findReservationTimeById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));
        Theme theme = themeQueryingDao.findThemeById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        Slot slot = Slot.create(request.date(), time, theme);

        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }

        Long slotId = slotDao.insert(slot);
        return slot.withId(slotId);
    }

    private void deleteSlot(long slotId) {
        long slotDeleted = slotDao.deleteIfNoWaiting(slotId);
        if (slotDeleted == 0) {
            throw new DataIntegrityViolationException("대기열이 변경되었습니다. 다시 시도해주세요.");
        }
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }
}
