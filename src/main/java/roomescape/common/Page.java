package roomescape.common;

import java.util.List;

public record Page<T>(int totalPages, int totalElements, List<T> content) {

    public static <T> Page<T> of(long totalElements, int size, List<T> content) {
        if (totalElements == 0) {
            return new Page<>(1, 0, content);
        }

        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new Page<>(totalPages, (int) totalElements, content);
    }
}
