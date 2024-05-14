package roomescape.service;

import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;
import static roomescape.service.mapper.ReservationTimeResponseMapper.toResponse;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.mapper.ReservationTimeResponseMapper;

@Service
@Transactional
public class ReservationTimeService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse save(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeRequest.startAt())) {
            throw new RoomescapeException(DUPLICATE_RESERVATION_TIME);
        }
        ReservationTime reservationTime = new ReservationTime(reservationTimeRequest.startAt());
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return toResponse(saved);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponseMapper::toResponse)
                .toList();
    }

    public void delete(long id) {
        validateUsedTime(id);
        reservationTimeRepository.delete(id);
    }

    private void validateUsedTime(long id) {
        reservationTimeRepository.findById(id).ifPresent(this::validateUsedTime);
    }

    private void validateUsedTime(ReservationTime reservationTime) {
        boolean existsByTime = reservationRepository.existsByTime(reservationTime);
        if (existsByTime) {
            throw new RoomescapeException(DELETE_USED_TIME);
        }
    }
}
