package roomescape.business.service;

import org.springframework.stereotype.Service;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;

@Service
public class ValidationService {

    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ValidationService(final ThemeRepository themeRepository,
                             final ReservationTimeRepository reservationTimeRepository) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }




}
