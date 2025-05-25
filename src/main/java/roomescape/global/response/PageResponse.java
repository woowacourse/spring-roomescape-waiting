package roomescape.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
