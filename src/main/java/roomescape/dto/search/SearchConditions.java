package roomescape.dto.search;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SearchConditions(@NotNull Long themeId, @NotNull Long memberId, @NotNull LocalDate dateFrom, @NotNull LocalDate dateTo) {

}
