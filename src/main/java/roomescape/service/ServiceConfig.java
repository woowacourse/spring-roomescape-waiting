package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.domain.ReservationDetailFactory;
import roomescape.domain.ReservationFactory;
import roomescape.domain.repository.ReservationDetailRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;

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
