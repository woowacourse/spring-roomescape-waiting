package roomescape.presentation.dto.request;

import java.time.LocalDate;

public record WaitingRequest(LocalDate date, Long themeId, Long timeId) {

}
