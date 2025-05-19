package roomescape.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import roomescape.dto.request.TimeRequest;
import roomescape.dto.response.TimeResponse;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;

@Service
public class TimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;

    public TimeService(JpaReservationTimeRepository reservationTimeRepository,
        JpaReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TimeResponse> findAllReservationTimes() {
        return reservationTimeRepository.findAll().stream()
            .map(TimeResponse::from)
            .toList();
    }

    public List<TimeResponse> findAllTimesWithBooked(LocalDate date, Long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAllByOrderByStartAtAsc();
        Set<Long> bookedTimeIds = new HashSet<>(
            reservationRepository.findBookedTimeIds(date, themeId));

        return allTimes.stream()
            .map(time -> TimeResponse.from(time, bookedTimeIds.contains(time.getId())))
            .toList();
    }

    public TimeResponse addReservationTime(TimeRequest request) {
        validateDuplicateTime(request);
        return TimeResponse.from(
            reservationTimeRepository.save(new ReservationTime(request.startAt())));
    }

    private void validateDuplicateTime(TimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedException("reservationTime");
        }
    }

    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
