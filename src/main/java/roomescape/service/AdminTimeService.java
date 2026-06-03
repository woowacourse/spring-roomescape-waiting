package roomescape.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.Time;
import roomescape.dto.TimeRequest;
import roomescape.dto.TimeResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.repository.TimeDao;

@Service
public class AdminTimeService {
    private final TimeDao timeDao;

    public AdminTimeService(TimeDao timeDao) {
        this.timeDao = timeDao;
    }

    public TimeResponse save(TimeRequest request) {
        try {
            Long id = timeDao.save(request.startAt());
            Time saved = new Time(id, request.startAt());
            return TimeResponse.from(saved);
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_TIME);
        }
    }

    public List<TimeResponse> findAll() {
        return timeDao.findAll().stream()
                .map(TimeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        try {
            timeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_RESERVED_TIME);
        }
    }
}
