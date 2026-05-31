package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryDao;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationUpdateDao;
import roomescape.repository.ReservationWaitingUpdateDao;
import roomescape.repository.ThemeQueryDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationQueryDao reservationQueryingDao;
    private final ReservationUpdateDao reservationUpdatingDao;
    private final ReservationTimeQueryDao reservationTimeQueryingDao;
    private final ThemeQueryDao themeQueryingDao;
    private final ReservationWaitingUpdateDao reservationWaitingUpdateDao;

    public ReservationService(ReservationQueryDao reservationQueryingDao, ReservationUpdateDao reservationUpdatingDao, ReservationTimeQueryDao reservationTimeQueryingDao, ThemeQueryDao themeQueryingDao, ReservationWaitingUpdateDao reservationWaitingUpdateDao) {
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationUpdatingDao = reservationUpdatingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
        this.reservationWaitingUpdateDao = reservationWaitingUpdateDao;
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

        Reservation reservationCommand = reservationReq.to(reservationTimeById, themeById);
        reservationCommand.validatePastDateTime();

        validateDuplicatedReservation(themeById.getId(), reservationReq.date(), reservationTimeById.getId());

        Reservation reservation = reservationReq.to(reservationTimeById, themeById);
        Long generatedId = reservationUpdatingDao.insert(reservation);
        return ReservationResponse.from(reservation.withReservationId(generatedId));
    }

    public ReservationResponse update(Long id, ReservationRequest reservationReq) {
        Reservation existedReservation = getReservation(id);
        existedReservation.validatePastDateTime();

        ReservationTime newTime = reservationTimeQueryingDao.findReservationTimeById(reservationReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationReq.timeId()));

        Reservation reservationCommand = reservationReq.to(newTime, null);
        reservationCommand.validatePastDateTime();
        validateDuplicatedReservation(existedReservation.getTheme().getId(), reservationReq.date(), newTime.getId());

        Reservation updatedReservation = existedReservation.withUpdatedDateAndTime(reservationReq.date(), newTime);

        reservationUpdatingDao.update(id, updatedReservation);
        return ReservationResponse.from(updatedReservation);
    }

    public void delete(Long id) {
        Reservation reservation = getReservation(id);
        reservation.validatePastDateTime();
        reservationUpdatingDao.delete(id);
    }

    private Reservation getReservation(Long id) {
        return reservationQueryingDao.findReservationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "번 예약이 존재하지 않습니다."));
    }

    private void validateDuplicatedReservation(Long themeId, LocalDate date, Long timeId) {
        Optional<Reservation> duplicateReservation = reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date, timeId);
        if (duplicateReservation.isPresent()) {
            throw new ReservationAlreadyExistException();
        }
    }
}
