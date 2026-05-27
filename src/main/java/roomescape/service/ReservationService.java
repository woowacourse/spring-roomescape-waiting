package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.response.ServiceReceptionResponse;

@Component
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation save(ServiceReservationCreateRequest request, ReservationTime reservationTime, Theme theme) {
        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        return reservationRepository.save(reservationWithoutId);
    }

    public List<ServiceReceptionResponse> findByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
    }

    public List<ServiceReceptionResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
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
        if (reservationRepository.existByThemeId(themeId)) {
            throw new CustomInvalidRequestException(ErrorCode.REFERENCED_THEME);
        }
    }

    public void validateReferencedTime(Long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new CustomInvalidRequestException(ErrorCode.REFERENCED_TIME);
        }
    }
}
