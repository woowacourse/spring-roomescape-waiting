package roomescape.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse<Void> success(SuccessCode code) {
        return new ApiResponse<>(code.getValue(), code.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return new ApiResponse<>(code.getValue(), code.getMessage(), data);
    }

    public static ApiResponse<Void> fail(ErrorCode code) {
        return new ApiResponse<>(code.getValue(), code.getMessage(), null);
    }
}
