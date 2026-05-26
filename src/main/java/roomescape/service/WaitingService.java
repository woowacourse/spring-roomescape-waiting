package roomescape.service;

import java.time.LocalDateTime;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.dto.WaitingRequest;
import roomescape.dto.WaitingResponse;
import roomescape.repository.ReservationDao;
import roomescape.repository.WaitingDao;

@Service
public class WaitingService {
    private final WaitingDao waitingDao;

    public WaitingService(WaitingDao waitingDao) {
        this.waitingDao = waitingDao;
    }

    public WaitingResponse save(LocalDateTime now, WaitingRequest request) {
        // Todo : 날짜에 대한 검증 필요
        long waitingId = waitingDao.save(request.name(), request.reservationId());
        int order =  waitingDao.findOrderByReservationId(waitingId, request.reservationId());
        Waiting waiting = new Waiting(waitingId, request.name(), request.reservationId());
        return WaitingResponse.from(waiting, order);
    }

}
