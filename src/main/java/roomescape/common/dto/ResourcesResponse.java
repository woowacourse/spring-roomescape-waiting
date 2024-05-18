package roomescape.common.dto;

import java.util.List;

public record ResourcesResponse<T>(List<T> resources) {
}
