package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.domain.reservationdetail.ReservationDetailFactory;
import roomescape.domain.reservation.ReservationFactory;
import roomescape.domain.reservationdetail.ReservationDetailRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdetail.ReservationTimeRepository;
import roomescape.domain.reservationdetail.ThemeRepository;

@Configuration
@RequiredArgsConstructor
public class ServiceConfig {
    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Bean
    public ReservationFactory reservationFactory() {
        return new ReservationFactory(reservationRepository);
    }

    @Bean
    public ReservationDetailFactory reservationDetailFactory() {
        return new ReservationDetailFactory(
                reservationDetailRepository,
                reservationTimeRepository,
                themeRepository
        );
    }
}
