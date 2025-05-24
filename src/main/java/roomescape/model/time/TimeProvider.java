package roomescape.model.time;

import java.time.LocalDateTime;

public interface TimeProvider {

    LocalDateTime getCurrentDateTime();
}
