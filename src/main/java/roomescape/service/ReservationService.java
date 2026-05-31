package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
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
        ReservationTime reservationTimeById = reservationTimeQueryingDao.findReservationTimeById(reservationReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationReq.timeId()));
        Theme themeById = themeQueryingDao.findThemeById(reservationReq.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationReq.themeId()));

        Reservation reservation = reservationReq.to(reservationTimeById, themeById);

        validateDuplicatedReservation(themeById.getId(), reservationReq.date(), reservationTimeById.getId());

        Long generatedId = reservationUpdatingDao.insert(reservation);
        return ReservationResponse.from(reservation.withReservationId(generatedId));
    }

    public ReservationResponse update(Long id, ReservationRequest reservationReq) {
        Reservation existedReservation = getReservation(id);

        ReservationTime newTime = reservationTimeQueryingDao.findReservationTimeById(reservationReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationReq.timeId()));
        Theme theme = themeQueryingDao.findThemeById(reservationReq.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationReq.themeId()));

        Reservation reservation = existedReservation.update(reservationReq.name(), reservationReq.date(), newTime, theme);
        validateDuplicatedReservation(existedReservation.getTheme().getId(), reservationReq.date(), newTime.getId());

        reservationUpdatingDao.update(id, reservation);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Reservation> optionalReservation = reservationQueryingDao.findReservationById(id);

        if(optionalReservation.isEmpty()) {
            return;
        }

        Optional<ReservationWaiting> optionalReservationWaiting = reservationWaitingDao.findFirstByReservationId(id);

        if(optionalReservationWaiting.isEmpty()) {
            reservationUpdatingDao.delete(id);
            return;
        }

        ReservationWaiting reservationWaiting = optionalReservationWaiting.get();

        reservationUpdatingDao.updateName(id, reservationWaiting.getName());
        reservationWaitingDao.delete(reservationWaiting.getId());
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약을 찾을 수 없습니다."));
    }

    private void validateDuplicatedReservation(Long themeId, LocalDate date, Long timeId) {
        Optional<Reservation> duplicateReservation = reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date,timeId);
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
    }
}
