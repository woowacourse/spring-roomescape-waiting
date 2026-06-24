package roomescape.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidReservationStateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.ResourceInUseException;
import roomescape.exception.UnauthorizedReservationException;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.PaymentGatewayConnectionException;
import roomescape.payment.exception.PaymentResultUnknownException;

@Component
public class CustomExceptionResolver implements HandlerExceptionResolver, Ordered {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Nullable
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         @Nullable Object handler, Exception ex) {
        try {
            if (ex instanceof PaymentAmountMismatchException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof PaymentGatewayConnectionException) {
                writeJsonResponse(response, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof PaymentResultUnknownException) {
                writeJsonResponse(response, HttpStatus.GATEWAY_TIMEOUT, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof DuplicateReservationException) {
                writeJsonResponse(response, HttpStatus.CONFLICT, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof ResourceInUseException) {
                writeJsonResponse(response, HttpStatus.CONFLICT, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof InvalidReservationStateException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof UnauthorizedReservationException) {
                writeJsonResponse(response, HttpStatus.FORBIDDEN, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof NotFoundException) {
                writeJsonResponse(response, HttpStatus.NOT_FOUND, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof IllegalArgumentException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof IllegalStateException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, ex.getMessage());
                return new ModelAndView();
            }

            if (ex instanceof DataIntegrityViolationException) {
                writeJsonResponse(response, HttpStatus.CONFLICT, "데이터 무결성 제약 조건을 위반했습니다.");
                return new ModelAndView();
            }

            if (ex instanceof DateTimeParseException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, "요청 시간 입력 형식이 올바르지 않습니다.");
                return new ModelAndView();
            }

            if (ex instanceof HttpMessageNotReadableException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
                return new ModelAndView();
            }

            if (ex instanceof MethodArgumentTypeMismatchException) {
                writeJsonResponse(response, HttpStatus.BAD_REQUEST, "요청 파라미터의 타입이 올바르지 않습니다.");
                return new ModelAndView();
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    private void writeJsonResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = objectMapper.writeValueAsString(Map.of("message", message));
        response.getWriter().write(body);
    }
}
