package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.reservation.application.dto.request.CreateReservationThemeServiceRequest;
import roomescape.reservation.application.dto.response.ReservationThemeServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.exception.ReservationException;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.service.ReservationThemeValidator;

@Service
@RequiredArgsConstructor
public class AdminReservationThemeService {

    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationThemeValidator reservationThemeValidator;

    public ReservationThemeServiceResponse create(CreateReservationThemeServiceRequest request) {
        ReservationTheme reservationTheme = reservationThemeRepository.save(request.toReservationTheme());
        return ReservationThemeServiceResponse.from(reservationTheme);
    }

    public void delete(Long id) {
        ReservationTheme reservationTheme = reservationThemeRepository.getById(id);
        try {
            reservationThemeValidator.validateNotInUse(id);
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }
        reservationThemeRepository.remove(reservationTheme);
    }
}
