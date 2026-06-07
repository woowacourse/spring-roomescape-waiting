package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.ReservationNotExistsException;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation save(Reservation reservationWithoutId) {
        return reservationRepository.save(reservationWithoutId);
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotExistsException::new);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(id);
    }

    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }

    public void validateReferencedTheme(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new CannotDeleteThemeInUseException();
        }
    }

    public void validateReferencedTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new CannotDeleteReservationTimeInUseException();
        }
    }
}
