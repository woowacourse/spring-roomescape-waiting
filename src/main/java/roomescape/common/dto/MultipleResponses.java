package roomescape.common.dto;

import java.util.List;

public record MultipleResponses<T>(List<T> responses) {
}
