package roomescape.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import roomescape.domain.event.CancelEventPublisher;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.infra.event.ReservationCancelEventPublisher;
import roomescape.infra.event.ReservationEventHandler;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class InfraConfiguration {
    private final ApplicationEventPublisher publisher;
    private final ReservationRepository reservationRepository;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("reservation-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public CancelEventPublisher eventPublisher() {
        return new ReservationCancelEventPublisher(publisher);
    }

    @Bean
    public ReservationEventHandler reservationEventHandler() {
        return new ReservationEventHandler(taskScheduler(), reservationRepository);
    }
}
