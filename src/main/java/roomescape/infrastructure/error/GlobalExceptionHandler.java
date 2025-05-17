package roomescape.infrastructure.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.infrastructure.error.exception.ForbiddenException;
import roomescape.infrastructure.error.exception.LoginAuthException;
import roomescape.infrastructure.error.exception.UnauthorizedException;
import roomescape.infrastructure.error.exception.AuthInfoResolveException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiFailResponse> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(new ApiFailResponse("예상치 못한 에러가 발생했습니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiFailResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ApiFailResponse(errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiFailResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException formatEx && formatEx.getTargetType() == LocalDate.class) {
            return ResponseEntity.badRequest().body(new ApiFailResponse("날짜는 yyyy-MM-dd 형식이어야 합니다."));
        }
        if (e.getCause() instanceof InvalidFormatException formatEx && formatEx.getTargetType() == LocalTime.class) {
            return ResponseEntity.badRequest().body(new ApiFailResponse("시간은 HH:mm 형식이어야 합니다."));
        }
        return ResponseEntity.badRequest().body(new ApiFailResponse("잘못된 요청입니다."));
    }

    @ExceptionHandler(AuthInfoResolveException.class)
    public ResponseEntity<ApiFailResponse> handleAuthInfoResolveException(AuthInfoResolveException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiFailResponse("인증 정보를 확인할 수 없습니다."));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiFailResponse> handleAuthException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiFailResponse("인증에 실패했습니다."));
    }

    @ExceptionHandler(LoginAuthException.class)
    public ResponseEntity<ApiFailResponse> handleLoginAuthException(LoginAuthException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiFailResponse("로그인에 실패했습니다. 아이디 또는 비밀번호를 다시 확인하세요"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiFailResponse> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiFailResponse("접근권한이 없습니다."));
    }
}
