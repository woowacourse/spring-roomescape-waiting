package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.command.CreateReservationTimeCommand;
import roomescape.dto.reservationtime.response.ReservationTimeResponses;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponses getReservationTimes(int page, int size) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll(size + 1, page * size);
        boolean hasNext = reservationTimes.size() > size;
        if (hasNext) {
            reservationTimes = reservationTimes.subList(0, size);
        }
        return ReservationTimeResponses.of(reservationTimes, hasNext);
    }

    public ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간을(를) 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public ReservationTime createReservationTime(CreateReservationTimeCommand command) {
        ReservationTime newReservationTime = new ReservationTime(null, command.startAt());
        Long newReservationTimeId = reservationTimeRepository.save(newReservationTime);
        return newReservationTime.withId(newReservationTimeId);
    }

    public void deleteReservationTime(Long id) {
        validateNotReferencedByReservation(id);
        int affected = reservationTimeRepository.deleteById(id);
        if (affected == 0) {
            throw new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간을(를) 찾을 수 없습니다. id=" + id);
        }
    }

    private void validateNotReferencedByReservation(Long id) {
        if (reservationRepository.existsByReservationTimeId(id)) {
            throw new RoomescapeException(ErrorType.RESERVATION_TIME_IN_USE,
                    "예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
