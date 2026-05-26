package roomescape.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.dto.WaitingRequest;
import roomescape.dto.WaitingResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationDao;
import roomescape.repository.WaitingDao;

@Service
public class WaitingService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;

    public WaitingService(WaitingDao waitingDao, ReservationDao reservationDao) {
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
    }

    public WaitingResponse save(LocalDateTime now, WaitingRequest request) {
        Reservation reservation = reservationDao.findById(request.reservationId());
        LocalDateTime time = LocalDateTime.of(reservation.getDate(), reservation.getTime().getStartAt());
        validateDateAndTimeNotPast(now,time);

        long waitingId = waitingDao.save(request.name(), request.reservationId());
        int order =  waitingDao.findOrderByReservationId(waitingId, request.reservationId());
        Waiting waiting = new Waiting(waitingId, request.name(), request.reservationId());
        return WaitingResponse.from(waiting, order);
    }

    public void delete(LocalDateTime now, Long id) {
        Waiting waiting = waitingDao.findById(id);
        Reservation reservation = reservationDao.findById(waiting.getReservationId());
        LocalDateTime time = LocalDateTime.of(reservation.getDate(), reservation.getTime().getStartAt());
        validateDateAndTimeNotPast(now, time);

        waitingDao.delete(id);
    }

    private void validateDateAndTimeNotPast(LocalDateTime now, LocalDateTime reservationTime) {
        if (now.isAfter(reservationTime)) {
            throw new CustomException(ErrorCode.PAST_DATE_RESERVATION);
        }
    }
}
