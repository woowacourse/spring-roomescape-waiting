package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;

@Component
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
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION));
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
            throw new CustomInvalidRequestException(ErrorCode.REFERENCED_THEME);
        }
    }

    public void validateReferencedTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new CustomInvalidRequestException(ErrorCode.REFERENCED_TIME);
        }
    }
}
