package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.TimeSlot;
import roomescape.dto.request.TimeSlotRequest;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.TimeSlotRepository;

@Service
public class TimeService {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;

    public TimeService(TimeSlotRepository timeSlotRepository, ReservationRepository reservationRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TimeSlotResponse> findAll() {
        return timeSlotRepository.findAll()
                .stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    public TimeSlotResponse create(TimeSlotRequest timeSlotRequest) {
        validateDuplicatedTime(timeSlotRequest);
        TimeSlot timeSlot = timeSlotRequest.toEntity();
        TimeSlot createdTimeSlot = timeSlotRepository.save(timeSlot);
        return TimeSlotResponse.from(createdTimeSlot);
    }

    public void delete(Long id) {
        validateExistReservation(id);
        timeSlotRepository.deleteById(id);
    }

    private void validateDuplicatedTime(TimeSlotRequest timeSlotRequest) {
        if (timeSlotRepository.existsByStartAt(timeSlotRequest.startAt())) {
            throw new IllegalArgumentException("[ERROR] 이미 등록된 시간입니다");
        }
    }

    private void validateExistReservation(Long id) {
        TimeSlot timeSlot = findTimeSlotById(id);
        if (reservationRepository.existsByTime(timeSlot)) {
            throw new IllegalArgumentException("[ERROR] 예약이 등록된 시간은 제거할 수 없습니다");
        }
    }

    private TimeSlot findTimeSlotById(long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 시간입니다"));
    }
}
