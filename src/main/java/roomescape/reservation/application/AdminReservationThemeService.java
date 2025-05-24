package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.CreateReservationThemeServiceRequest;
import roomescape.reservation.application.dto.response.ReservationThemeServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.service.ReservationThemeOperation;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationThemeService {

    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationThemeOperation reservationThemeOperation;

    @Transactional
    public ReservationThemeServiceResponse create(CreateReservationThemeServiceRequest request) {
        ReservationTheme reservationTheme = reservationThemeRepository.save(request.toReservationTheme());
        return ReservationThemeServiceResponse.from(reservationTheme);
    }

    @Transactional
    public void delete(Long id) {
        ReservationTheme reservationTheme = reservationThemeRepository.getById(id);
        reservationThemeOperation.removeTheme(reservationTheme);
    }
}
