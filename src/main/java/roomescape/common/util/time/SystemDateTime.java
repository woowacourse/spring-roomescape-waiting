package roomescape.common.util.time;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemDateTime implements DateTime {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
