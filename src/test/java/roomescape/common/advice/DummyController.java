package roomescape.common.advice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.CustomException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;

@RestController
@Validated
public class DummyController {

    @PostMapping("/dummy")
    public ResponseEntity<Long> testMethod(@Valid @RequestBody DummyDto.DummyData body) {
        return ResponseEntity.ok(body.testField());
    }

    @PostMapping("/dummy/{data}")
    public ResponseEntity<Long> testMethod(@Positive(message = "양수가 아님") @PathVariable Long data) {
        return ResponseEntity.ok(data);
    }

    @GetMapping("/dummy/param")
    public void requestParamCheck(@RequestParam String test) {
    }

    @GetMapping("/dummy/business")
    public void business() {
        throw new BusinessException();
    }

    @GetMapping("/dummy/forbidden")
    public void forbidden() {
        throw new ForbiddenException("접근 권한이 없습니다.");
    }

    @GetMapping("/dummy/entityNotFound")
    public void entityNotFound() {
        throw new NotFoundException("데이터 없음");
    }

    @GetMapping("/dummy/duplicateEntity")
    public void duplicateEntity() {
        throw new ConflictException("충돌");
    }

    @GetMapping("/dummy/dataIntegrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("무결성");
    }

    @GetMapping("/dummy/internal")
    public void internal() {
        throw new RuntimeException();
    }

    @GetMapping("/api/admin")
    public void accessCheck() {
    }

    @GetMapping("/api/admin/auth/test")
    public void loginCheck() {
    }

    private static class BusinessException extends CustomException {

        public BusinessException() {
            super("BUSINESS_ERROR", HttpStatus.BAD_REQUEST, "비즈니스 예외");
        }
    }
}
