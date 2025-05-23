package roomescape.reservationTime.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationTime.controller.dto.request.TimeRequest;
import roomescape.reservationTime.entity.ReservationTime;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.NotFoundException;
import roomescape.reservationTime.repository.JpaReservationTimeRepository;

@Service
public class TimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;

    public TimeService(JpaReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAllTimesWithBooked(LocalDate date, long themeId) {
        return reservationTimeRepository.findAllTimesWithBooked(date, themeId);
    }

    @Transactional
    public ReservationTime addReservationTime(TimeRequest request) {
        validateDuplicateTime(request);
        return reservationTimeRepository.save(new ReservationTime(request.startAt(), false));
    }

    @Transactional(readOnly = true)
    private void validateDuplicateTime(TimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedException("reservationTime");
        }
    }

    @Transactional
    public void removeReservationTime(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NotFoundException("reservationTime");
        }
        reservationTimeRepository.deleteById(id);
    }
}
