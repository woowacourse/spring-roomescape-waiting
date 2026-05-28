package roomescape.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.ReservationTime;
import roomescape.dto.TimeRequest;
import roomescape.dto.TimeResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationTimeDao;

@Service
public class AdminTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public AdminTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public TimeResponse save(TimeRequest request) {
        try {
            Long id = reservationTimeDao.save(request.startAt());
            return TimeResponse.from(new ReservationTime(id, request.startAt()));
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_TIME);
        }
    }

    public List<TimeResponse> findAll() {
        return reservationTimeDao.findAll().stream()
                .map(TimeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        try {
            reservationTimeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_RESERVED_TIME);
        }
    }
}
