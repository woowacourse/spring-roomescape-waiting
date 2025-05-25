package roomescape.service.timeprovider;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class CurrentTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

}
