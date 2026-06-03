package roomescape.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(Include.NON_NULL)
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(
            String field,
            String reason
    ) {
    }
}
