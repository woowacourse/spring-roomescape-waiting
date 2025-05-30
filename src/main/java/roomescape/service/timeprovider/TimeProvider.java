package roomescape.service.timeprovider;

import java.time.LocalDateTime;

public interface TimeProvider {

    LocalDateTime getCurrentDateTime();
}
