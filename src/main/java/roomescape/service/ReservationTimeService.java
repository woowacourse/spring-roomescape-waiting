package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.custom.CannotReadPastReservationTimeAvailability;
import roomescape.exception.custom.ReservationTimeAlreadyExistsException;
import roomescape.exception.custom.ReservationTimeNotExistsException;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationTime save(ReservationTime reservationTimeWithoutId) {
        validateDuplicatedReservationTime(reservationTimeWithoutId.getStartAt());

        return reservationTimeRepository.save(reservationTimeWithoutId);
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    private void validateDuplicatedReservationTime(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new ReservationTimeAlreadyExistsException();
        }
    }

    public List<ReservationTime> findReservedTimesByDateAndTheme(LocalDate date, Long themeId) {
        validateNotPastDate(date);

        return reservationTimeRepository.findReservedTimesByDateAndTheme_Id(date, themeId);
    }

    private void validateNotPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now(clock))) {
            throw new CannotReadPastReservationTimeAvailability();
        }
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeRepository.deleteById(id);
    }

    public ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(ReservationTimeNotExistsException::new);
    }
}
