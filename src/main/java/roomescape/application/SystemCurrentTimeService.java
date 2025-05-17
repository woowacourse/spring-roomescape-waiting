package roomescape.application;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemCurrentTimeService implements CurrentTimeService {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
