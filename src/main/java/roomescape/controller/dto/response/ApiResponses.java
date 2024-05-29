package roomescape.controller.dto.response;

import java.util.Collection;

public record ApiResponses<T>(Collection<T> list) {
}
