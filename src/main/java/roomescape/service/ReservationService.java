package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationUpdatingDao;
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

    public ReservationService(ReservationQueryingDao reservationQueryingDao, ReservationUpdatingDao reservationUpdatingDao, ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao) {
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
    }

    public ReservationResponse read(Long id) {
        Reservation reservationById = reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
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

        if (reservationReq.date().isBefore(LocalDate.now())) {
            throw new InvalidReservationException();
        }

        Optional<Reservation> savedReservation = reservationQueryingDao.findReservationByThemeAndDateAndTime(themeById.getId(), reservationReq.date(), reservationTimeById.getId());
        if (savedReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }

        Reservation reservation = new Reservation(reservationReq.name(), reservationReq.date(), reservationTimeById, themeById);
        Long generatedId = reservationUpdatingDao.insert(reservation);
        return ReservationResponse.from(reservation.reservationWithId(generatedId));
    }

    public ReservationResponse update(Long id, ReservationRequest reservationReq) {
        Reservation existedReservation = reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        ReservationTime newTime = reservationTimeQueryingDao.findReservationTimeById(reservationReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationReq.timeId()));

        if (reservationReq.date().isBefore(LocalDate.now())) {
            throw new InvalidReservationException();
        }

        Optional<Reservation> duplicateReservation = reservationQueryingDao.findReservationByThemeAndDateAndTime(existedReservation.getTheme().getId(), reservationReq.date(), newTime.getId());
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
        Reservation updatedReservation = existedReservation.withUpdatedDateAndTime(reservationReq.date(), newTime);
        reservationUpdatingDao.update(id,  updatedReservation);
        return ReservationResponse.from(updatedReservation);
    }

    public void delete(Long id) {
        reservationUpdatingDao.delete(id);
    }
}
