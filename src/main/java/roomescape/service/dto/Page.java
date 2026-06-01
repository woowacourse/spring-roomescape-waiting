package roomescape.service.dto;

import java.util.List;

public record Page<T>(List<T> content, long totalCount) {
}