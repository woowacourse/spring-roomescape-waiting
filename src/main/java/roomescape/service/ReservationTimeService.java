package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.dto.reservationtime.AvailableReservationTimeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationTimeUpdatingDao;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ReservationTimeUpdatingDao reservationTimeUpdatingDao;

    public ReservationTimeService(ReservationTimeQueryingDao reservationTimeQueryingDao, ReservationTimeUpdatingDao reservationTimeUpdatingDao) {
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.reservationTimeUpdatingDao = reservationTimeUpdatingDao;
    }

    public List<ReservationTimeResponse> readAll() {
        return reservationTimeQueryingDao.findAllReservationTime().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> readAvailable(LocalDate date, Long themeId) {
        return reservationTimeQueryingDao.findAvailableReservationTime(date, themeId).stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeReq) {
        Long generatedId = reservationTimeUpdatingDao.insert(reservationTimeReq);
        return ReservationTimeResponse.from(new ReservationTime(generatedId, reservationTimeReq.startAt()));
    }

    public void update(ReservationTimeRequest newReservationTimeReq, Long id) {
        reservationTimeUpdatingDao.save(id, newReservationTimeReq);
    }

    public void delete(Long id) {
        try {
            reservationTimeUpdatingDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
