package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.AlreadyInUseException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<TimeSlotResponse> findAvailableTime(Long themeId, LocalDate date) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Reservations reservations = reservationRepository.findByDateAndThemeId(date, themeId);

        return reservations.toTimeSlots(allTimes).stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    @Transactional
    public ReservationTimeResponse save(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new AlreadyExistsException("이미 존재하는 시간이므로 추가할 수 없습니다.");
        }

        return ReservationTimeResponse.from(reservationTimeRepository.save(new ReservationTime(request.startAt())));
    }

    @Transactional
    public void delete(Long id) {
        if (reservationTimeRepository.existsByTimeId(id)) {
            throw new AlreadyInUseException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.delete(id);
    }
}
