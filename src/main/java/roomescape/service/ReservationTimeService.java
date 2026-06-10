package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.dto.reservationtime.AvailableReservationTimeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.repository.ReservationTimeQueryDao;
import roomescape.repository.ReservationTimeUpdateDao;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeQueryDao reservationTimeQueryDao;
    private final ReservationTimeUpdateDao reservationTimeUpdateDao;

    public ReservationTimeService(ReservationTimeQueryDao reservationTimeQueryDao, ReservationTimeUpdateDao reservationTimeUpdateDao) {
        this.reservationTimeQueryDao = reservationTimeQueryDao;
        this.reservationTimeUpdateDao = reservationTimeUpdateDao;
    }

    public List<ReservationTimeResponse> readAll() {
        return reservationTimeQueryDao.findAllReservationTime()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> readAvailable(LocalDate date, Long themeId) {
        return reservationTimeQueryDao.findAvailableReservationTime(date, themeId)
                .stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeReq) {
        Long generatedId = reservationTimeUpdateDao.insert(reservationTimeReq);
        return ReservationTimeResponse.from(new ReservationTime(generatedId, reservationTimeReq.startAt()));
    }

    public void update(ReservationTimeRequest newReservationTimeReq, Long id) {
        reservationTimeUpdateDao.save(id, newReservationTimeReq);
    }

    public void delete(Long id) {
        try {
            reservationTimeUpdateDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
