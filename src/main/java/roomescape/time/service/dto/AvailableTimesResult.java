package roomescape.time.service.dto;

import java.util.List;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public record AvailableTimesResult(List<AvailableTimeQueryResult> availableTimeQueryResults) {
}
