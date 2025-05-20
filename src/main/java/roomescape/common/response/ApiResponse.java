package roomescape.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResponse<T> createSuccess(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<?> createSuccessWithNoData() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<?> createError(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
