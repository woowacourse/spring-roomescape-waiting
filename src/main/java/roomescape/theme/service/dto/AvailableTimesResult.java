package roomescape.theme.service.dto;

import java.util.List;
import roomescape.time.repository.AvailableTimeQueryResult;

public record AvailableTimesResult(List<AvailableTimeQueryResult> availableTimeQueryResults) {
}
