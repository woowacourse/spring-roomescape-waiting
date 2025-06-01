package roomescape.waiting.dto;

import java.time.LocalDate;

public record WaitingCreateRequest(
        LocalDate date,
        Long theme,
        Long time
) {

}
