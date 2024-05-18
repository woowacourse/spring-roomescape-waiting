package roomescape.service.dto.response;

import java.util.List;

public record ListResponse<T>(List<T> responses, int count) {

    public ListResponse(List<T> items) {
        this(items, items.size());
    }
}
