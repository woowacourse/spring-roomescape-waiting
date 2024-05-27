package roomescape.common.dto;

import java.util.Collection;

public record MultipleResponses<T>(Collection<T> responses) {
}
