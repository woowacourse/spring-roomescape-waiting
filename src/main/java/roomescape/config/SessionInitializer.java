package roomescape.config;

import java.time.LocalDate;
import java.util.stream.IntStream;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import roomescape.service.SessionService;

@Component
public class SessionInitializer implements ApplicationRunner {

    private static final int DAYS = 14;

    private final SessionService sessionService;

    public SessionInitializer(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDate today = LocalDate.now();
        IntStream.range(0, DAYS)
                .mapToObj(today::plusDays)
                .forEach(sessionService::createSessionsForDate);
    }
}
