package roomescape.common.api;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }
}
