package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationWaitingDao;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationQueryingDao reservationQueryingDao;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao, ReservationQueryingDao reservationQueryingDao) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationQueryingDao = reservationQueryingDao;
    }

    public ReservationWaitingResponse create(ReservationWaitingRequest reservationWaitingReq) {
        Reservation reservation = getReservationByThemeAndDateAndTime(reservationWaitingReq.themeId(), reservationWaitingReq.date(), reservationWaitingReq.timeId());

        ReservationWaiting reservationWaitingCommand = reservationWaitingReq.to(reservation);

        if(reservationWaitingDao.isExistByNameAndReservationId(reservationWaitingReq.name(), reservation.getId())) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        try {
            Long id = reservationWaitingDao.create(reservationWaitingCommand);
            return ReservationWaitingResponse.from(reservationWaitingDao.findReservationWaitingById(id).get());
        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }
    }

    public void delete(Long id) {
        reservationWaitingDao.delete(id);
    }

    public List<ReservationWaitingResponse> readAll() {
        return reservationWaitingDao.findAllReservationWaiting()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public List<ReservationWaitingResponse> readByName(String name) {
        return reservationWaitingDao.findAllByName(name)
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    private Reservation getReservationByThemeAndDateAndTime(Long themeId, LocalDate date, Long timeId) {
        return reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date, timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약이 존재하지 않습니다."));
    }
}
