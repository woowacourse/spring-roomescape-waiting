package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.*;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;

import static roomescape.exception.ExceptionDomainType.*;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationCreateValidator reservationCreateValidator;

    public ReservationService(final ReservationDao reservationDao,
                              final ReservationCreateValidator reservationCreateValidator) {
        this.reservationDao = reservationDao;
        this.reservationCreateValidator = reservationCreateValidator;
    }

    public ReservationOutput createReservation(final ReservationInput input) {
        final Reservation reservation = reservationCreateValidator.validateReservationInput(input);
        final Reservation savedReservation = reservationDao.create(reservation);
        return ReservationOutput.toOutput(savedReservation);
    }

    public List<ReservationOutput> getAllReservations() {
        final List<Reservation> reservations = reservationDao.getAll();
        return ReservationOutput.toOutputs(reservations);
    }

    public List<ReservationOutput> searchReservation(final ReservationSearchInput input) {
        final List<Reservation> themeReservations = reservationDao.getThemeAndMemberReservationWithPeriod(
                input.themeId(), input.memberId(), input.fromDate(), input.toDate());
        return ReservationOutput.toOutputs(themeReservations);
    }

    public void deleteReservation(final long id) {
        if (reservationDao.delete(id)) {
            return;
        }
        throw new NotExistException(RESERVATION, id);
    }
}
