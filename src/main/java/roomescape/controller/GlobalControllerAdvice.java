package roomescape.controller;

import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.infrastructure.log.logger.RoomEscapeLog;

@RestControllerAdvice
public class GlobalControllerAdvice {

    private final RoomEscapeLog roomEscapeLog;

    public GlobalControllerAdvice(RoomEscapeLog roomEscapeLog) {
        this.roomEscapeLog = roomEscapeLog;
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        roomEscapeLog.printLog(e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(exception = MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleIllegalArgumentException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        roomEscapeLog.printLog(errorMessage);
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(exception = Exception.class)
    public ResponseEntity<String> handleIllegalArgumentException(Exception e) {
        roomEscapeLog.printLog(e.getMessage());
        return ResponseEntity.badRequest().body("예기치 못한 예외가 발생했습니다.");
    }

    @ExceptionHandler(exception = JwtException.class)
    public ResponseEntity<String> handleIllegalArgumentException(JwtException e) {
        roomEscapeLog.printLog(e.getMessage());
        return ResponseEntity.badRequest().body("재로그인이 필요합니다.");
    }
}
