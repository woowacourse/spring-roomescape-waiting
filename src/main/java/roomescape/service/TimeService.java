package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.request.TimeRequest;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaReservationTimeRepository;

@Service
public class TimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;

    public TimeService(JpaReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId) {
        return reservationTimeRepository.findAllTimesWithBooked(date, themeId);
    }

    public ReservationTime addReservationTime(TimeRequest request) {
        validateDuplicateTime(request);
        return reservationTimeRepository.save(new ReservationTime(request.startAt(), false));
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
