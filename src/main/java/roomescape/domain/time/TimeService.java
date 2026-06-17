package roomescape.domain.time;


import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.ReservationDao;
import roomescape.dao.TimeDao;
import roomescape.dto.request.TimeRequestDto;

@Service
@Transactional
public class TimeService {
    private final TimeDao timeDao;
    private final ReservationDao reservationDao;

    public TimeService(TimeDao timeDao, ReservationDao reservationDao) {
        this.timeDao = timeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional(readOnly = true)
    public List<Time> findAll() {
        return timeDao.findAll();
    }

    @Transactional(readOnly = true)
    public Time findById(Long id) {
        return timeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
    }

    public Time create(TimeRequestDto timeRequest) {
        if (timeDao.existsByStartAt(timeRequest.startAt())) {
            throw new DuplicateEntityException("이미 존재하는 시간 입니다.");
        }
        Time time = new Time(timeRequest.startAt());
        return timeDao.insert(time);
    }

    public void delete(Long id) {
        if (reservationDao.existsByTimeId(id)) {
            throw new BusinessRuleViolationException("예약이 존재하여 시간을 삭제할 수 없습니다.");
        }
        if (!timeDao.delete(id)) {
            throw new EntityNotFoundException("존재하지 않는 시간입니다.");
        }
    }
}
