package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.controller.dto.response.ErrorResponse;
import roomescape.domain.exception.DomainConflictException;
import roomescape.domain.exception.DomainRuleViolationException;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.BusinessException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 조회_결과가_없는_예외는_404로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND)
        );

        assertResponse(response, HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다.");
    }

    @Test
    void 비즈니스_충돌_예외는_409로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessConflict(
                new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION)
        );

        assertResponse(response, HttpStatus.CONFLICT, "DUPLICATE_RESERVATION", "이미 예약된 시간입니다.");
    }

    @Test
    void 일반_비즈니스_예외는_400으로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.INVALID_INPUT)
        );

        assertResponse(response, HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다.");
    }

    @Test
    void 도메인_충돌_예외는_409로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleDomainConflict(
                new DomainConflictException("이미 예약된 시간입니다.")
        );

        assertResponse(response, HttpStatus.CONFLICT, "DOMAIN_CONFLICT", "이미 예약된 시간입니다.");
    }

    @Test
    void 도메인_규칙_위반_예외는_400으로_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleDomainRuleViolation(
                new DomainRuleViolationException("예약자 이름은 비어 있을 수 없습니다.")
        );

        assertResponse(response, HttpStatus.BAD_REQUEST, "DOMAIN_RULE_VIOLATION", "예약자 이름은 비어 있을 수 없습니다.");
    }

    private void assertResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus status,
            String code,
            String message
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(code);
        assertThat(response.getBody().message()).isEqualTo(message);
    }
}
