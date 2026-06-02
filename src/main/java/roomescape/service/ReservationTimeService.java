package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        return reservationTimeRepository.insert(new ReservationTime(null, startAt));
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        deleteReservationTime(id);
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomescapeException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }

    private void deleteReservationTime(Long id) {
        try {
            reservationTimeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(
                    ErrorCode.RESOURCE_IN_USE,
                    "예약이 존재하는 시간은 삭제할 수 없습니다."
            );
        }
    }
}
