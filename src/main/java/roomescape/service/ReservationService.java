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
        return createReservation(reservationReq);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existed = getReservation(id);

        if (existed.isSameSlot(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            Reservation renamed = existed.update(reservationRequest.name());
            reservationUpdatingDao.updateName(id, reservationRequest.name());
            return ReservationResponse.from(renamed);
        }

        cancel(existed);
        return createReservation(reservationRequest);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Reservation> optionalReservation = reservationQueryingDao.findReservationById(id);
        if (optionalReservation.isEmpty()) {
            return;
        }
        Reservation reservation = optionalReservation.get();
        if (reservation.isExpired()) {
            throw new ExpiredDateTimeException();
        }
        cancel(reservation);
    }

    private void cancel(Reservation reservation) {
        long deleted = reservationUpdatingDao.delete(reservation.getId());
        if (deleted == 0) {
            return;
        }
        Long slotId = reservation.getSlot().getId();

        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.findFirstBySlotId(slotId);
        if (firstWaiting.isEmpty()) {
            long slotDeleted = slotDao.deleteIfNoWaiting(slotId);
            if (slotDeleted == 0) {
                throw new DataIntegrityViolationException("대기열이 변경되었습니다. 다시 시도해주세요.");
            }
            return;
        }

        ReservationWaiting waiting = firstWaiting.get();
        long claimed = reservationWaitingDao.delete(waiting.getId());
        if (claimed == 0) {
            throw new DataIntegrityViolationException("대기열이 변경되었습니다. 다시 시도해주세요.");
        }
        reservationUpdatingDao.insert(waiting.promote());
    }

    private ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeQueryingDao.findReservationTimeById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));
        Theme theme = themeQueryingDao.findThemeById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        Slot slot = Slot.create(request.date(), time, theme);
        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }
        if (slotDao.findByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId()).isPresent()) {
            throw new ReservationAlreadyExistException();
        }

        try {
            Long slotId = slotDao.insert(slot);
            Reservation reservation = Reservation.create(request.name(), slot.withId(slotId));
            Long reservationId = reservationUpdatingDao.insert(reservation);
            return ReservationResponse.from(reservation.withId(reservationId));
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }
}
