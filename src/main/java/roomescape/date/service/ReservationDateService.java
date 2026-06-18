package roomescape.date.service;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.repository.ReservationDateRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationDateService {

    private final ReservationDateRepository reservationDateRepository;

    public ReservationDate readDate(Long id) {
        return getReservationDate(id);
    }

    public List<ReservationDate> readDates() {
        return reservationDateRepository.findAll();
    }

    public List<ReservationDate> readDatesAfterToday() {
        LocalDate today = LocalDate.now();
        return reservationDateRepository.findAllByDateAfter(today);
    }

    @Transactional
    public ReservationDate register(LocalDate date) {
        validateDuplicateDateExist(date);
        return reservationDateRepository.save(ReservationDate.create(date));
    }

    @Transactional
    public ReservationDate updateStatus(Long dateId, boolean isActive) {
        ReservationDate reservationDate = getReservationDate(dateId);
        reservationDate.updateStatus(isActive);
        return reservationDate;
    }

    private ReservationDate getReservationDate(Long id) {
        return reservationDateRepository.findById(id)
            .orElseThrow(() -> new ReservationDateException(DATE_NOT_FOUND));
    }

    private void validateDuplicateDateExist(LocalDate date) {
        if (reservationDateRepository.existsByDate(date)) {
            throw new ReservationDateException(DATE_ALREADY_EXISTS);
        }
    }

}
