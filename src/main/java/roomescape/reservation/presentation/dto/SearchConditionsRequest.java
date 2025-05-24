package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SearchConditionsRequest(@NotNull Long themeId, @NotNull Long memberId, @NotNull LocalDate dateFrom,
                                      @NotNull LocalDate dateTo) {

}
