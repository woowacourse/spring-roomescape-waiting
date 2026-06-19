package roomescape.common;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(
                content.stream().map(mapper).toList(),
                totalElements,
                totalPages,
                page,
                size
        );
    }
}
