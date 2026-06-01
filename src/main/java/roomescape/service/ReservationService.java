package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation save(ServiceReservationCreateRequest request, ReservationTime reservationTime, Theme theme) {
        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
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
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_RESERVATION));
    }

    public void delete(Long id) {
        reservationRepository.delete(id);
    }

    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }

    public void validateReferencedTheme(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomEscapeException(DomainErrorCode.REFERENCED_THEME);
        }
    }

    public void validateReferencedTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomEscapeException(DomainErrorCode.REFERENCED_TIME);
        }
    }
}
