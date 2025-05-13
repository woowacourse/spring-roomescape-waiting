package roomescape.dto.search;

import java.time.LocalDate;

public record SearchConditions(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {

}
