package roomescape.dto.response;

import java.util.List;

public record MultipleResponse<T>(List<T> items) {
}
