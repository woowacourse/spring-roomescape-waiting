package roomescape.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String path;
    private final String code;
    private final String message;
    private final List<String> messages;
    private final LocalDateTime timeStamp;

    private ErrorResponse(String path, String code, String message, List<String> messages) {
        this.path = path;
        this.code = code;
        this.message = message;
        this.messages = messages;
        this.timeStamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String path, String code, String message) {
        return new ErrorResponse(path, code, message, null);
    }

    public static ErrorResponse of(String path, String code, List<String> messages) {
        List<String> copiedMessages = List.copyOf(messages);
        return new ErrorResponse(path, code, null, copiedMessages);
    }
}
