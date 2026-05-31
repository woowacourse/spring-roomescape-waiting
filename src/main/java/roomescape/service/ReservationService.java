package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ReservationWaitingDao;
import roomescape.repository.ThemeQueryingDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationQueryingDao reservationQueryingDao;
    private final ReservationUpdatingDao reservationUpdatingDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;
    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationService(ReservationQueryingDao reservationQueryingDao, ReservationUpdatingDao reservationUpdatingDao,
                              ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao, ReservationWaitingDao reservationWaitingDao) {
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public ReservationResponse read(Long id) {
        Reservation reservationById = getReservation(id);
        return ReservationResponse.from(reservationById);
    }

    public List<ReservationResponse> readAll() {
        List<Reservation> reservations = reservationQueryingDao.findAllReservations();
         return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readByName(String name) {
        return reservationQueryingDao.findAllByName(name)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(ReservationRequest reservationReq) {
        return createReservation(reservationReq);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationRequest reservationRequest) {
        Reservation existedReservation = getReservation(id);

        if (isSlotChanged(existedReservation, reservationRequest)) {
            return deleteAndCreateReservation(reservationRequest, existedReservation);
        }

        Reservation reservation = existedReservation.update(reservationRequest.name(), existedReservation.getDate(), existedReservation.getTime(), existedReservation.getTheme());

        long updatedRows = reservationUpdatingDao.updateIfVersion(id, existedReservation.getVersion(), reservation);
        if (updatedRows == 0) {
            throw new DataIntegrityViolationException("동시 수정으로 인해 예약을 변경할 수 없습니다. 다시 시도해주세요.");
        }
        return ReservationResponse.from(reservation.withReservationId(id));
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
        deleteReservation(reservation);
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }

    private ReservationResponse deleteAndCreateReservation(ReservationRequest reservationRequest, Reservation existedReservation) {
        if (!deleteReservation(existedReservation)) {
            throw new DataIntegrityViolationException("예약을 변경할 수 없습니다. 다시 시도해주세요.");
        }
        return createReservation(reservationRequest);
    }

    private ReservationResponse createReservation(ReservationRequest reservationRequest) {
        ReservationTime reservationTimeById = reservationTimeQueryingDao.findReservationTimeById(reservationRequest.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationRequest.timeId()));
        Theme themeById = themeQueryingDao.findThemeById(reservationRequest.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationRequest.themeId()));

        Reservation reservation = reservationRequest.to(reservationTimeById, themeById);

        validateDuplicatedReservation(themeById.getId(), reservationRequest.date(), reservationTimeById.getId());

        try {
            Long generatedId = reservationUpdatingDao.insert(reservation);
            return ReservationResponse.from(reservation.withReservationId(generatedId));
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistException();
        }
    }

    private boolean deleteReservation(Reservation existedReservation) {
        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.findFirstByReservationId(existedReservation.getId());
        if (firstWaiting.isEmpty()) {
            reservationUpdatingDao.delete(existedReservation.getId());
            return true;
        }

        ReservationWaiting waiting = firstWaiting.get();
        long updatedRow = reservationUpdatingDao.updateIfVersion(existedReservation.getId(), existedReservation.getVersion(), waiting.promote());
        if (updatedRow == 0) {
            return false;
        }

        long deletedRow = reservationWaitingDao.delete(waiting.getId());
        if (deletedRow == 0) {
            throw new DataIntegrityViolationException("예약 데이터를 삭제할 수 없습니다.");
        }
        return true;
    }

    private boolean isSlotChanged(Reservation existed, ReservationRequest req) {
        return !(existed.getDate().equals(req.date()) && existed.getTime().getId().equals(req.timeId()) && existed.getTheme().getId().equals(req.themeId()));
    }

    private void validateDuplicatedReservation(Long themeId, LocalDate date, Long timeId) {
        Optional<Reservation> duplicateReservation = reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date,timeId);
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
    }

}
