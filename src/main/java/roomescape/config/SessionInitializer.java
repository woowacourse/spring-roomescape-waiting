package roomescape.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import roomescape.service.SessionService;

import java.time.LocalDate;
import java.util.stream.LongStream;

@Component
public class SessionInitializer implements ApplicationRunner {

    private final SessionService sessionService;

    public SessionInitializer(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDate today = LocalDate.now();
        LongStream.rangeClosed(-7, 7)
                .mapToObj(today::plusDays)
                .forEach(sessionService::createSessionsForDate);
    }
}
