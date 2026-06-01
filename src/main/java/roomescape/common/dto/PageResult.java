package roomescape.common.dto;

import java.util.List;
import java.util.function.Function;

public record PageResult<T> (
        List<T> contents,
        long page,
        long size,
        long numberOfElements,
        long totalElements,
        long totalPages,
        boolean hasPrevious,
        boolean hasNext
) {

    public static <T> PageResult<T> of(List<T> contents, long page, long size, long totalElements) {
        long totalPages = (long) Math.ceil((double) totalElements / size);

        return new PageResult<>(
                contents,
                page,
                size,
                contents.size(),
                totalElements,
                totalPages,
                page > 1,
                page < totalPages
        );
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedContents = contents.stream()
                .map(mapper)
                .toList();

        return new PageResult<>(
                mappedContents,
                page,
                size,
                numberOfElements,
                totalElements,
                totalPages,
                hasPrevious,
                hasNext
        );
    }
}
