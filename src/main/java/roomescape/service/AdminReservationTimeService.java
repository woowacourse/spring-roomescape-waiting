package roomescape.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.TimeRequest;
import roomescape.dto.TimeResponse;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationTimeDao;

@Service
@Transactional(readOnly = true)
public class AdminReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public AdminReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    @Transactional
    public TimeResponse save(TimeRequest request) {
        try {
            Long id = reservationTimeDao.save(request.startAt());
            return TimeResponse.from(new ReservationTime(id, request.startAt()));
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION_TIME, "이미 존재하는 시간은 저장할 수 없습니다.");
        }
    }

    public List<TimeResponse> findAll() {
        return reservationTimeDao.findAll().stream()
                .map(TimeResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        try {
            reservationTimeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY, "예약중인 시간은 삭제할 수 없습니다.");
        }
    }
}
