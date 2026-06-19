package roomescape.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.Time;
import roomescape.domain.repository.TimeRepository;
import roomescape.dto.TimeRequest;
import roomescape.dto.TimeResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Service
public class AdminTimeService {
    private final TimeRepository timeRepository;

    public AdminTimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public TimeResponse save(TimeRequest request) {
        try {
            Time time = new Time(request.startAt());
            timeRepository.save(time);
            return TimeResponse.from(time);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_TIME);
        }
    }

    public List<TimeResponse> findAll() {
        return timeRepository.findAll().stream()
                .map(TimeResponse::from)
                .toList();
    }

    public void delete(long id) {
        try {
            timeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_RESERVED_TIME);
        }
    }
}
