package roomescape.common.advice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exception.CustomException;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;

@Validated
@RestController
class DummyController {

    @PostMapping("/dummy")
    Map<String, String> dummy(@Valid @RequestBody DummyDto request) {
        return Map.of("testField", request.testField());
    }

    @PostMapping("/dummy/{id}")
    void dummyPath(@PathVariable @Positive(message = "양수가 아님") Long id) {
    }

    @GetMapping("/dummy/business")
    void business() {
        throw new BusinessException("비즈니스 예외");
    }

    @GetMapping("/dummy/forbidden")
    void forbidden() {
        throw new ForbiddenException("접근 권한이 없습니다.");
    }

    @GetMapping("/dummy/entityNotFound")
    void notFound() {
        throw new NotFoundException("데이터 없음");
    }

    @GetMapping("/dummy/duplicateEntity")
    void duplicate() {
        throw new DuplicateException("충돌");
    }

    @GetMapping("/dummy/param")
    void param(@RequestParam String test) {
    }

    @GetMapping("/dummy/internal")
    void internal() {
        throw new RuntimeException("처리하지 않은 예외");
    }

    static class BusinessException extends CustomException {

        BusinessException(String message) {
            super("BUSINESS_EXCEPTION", HttpStatus.BAD_REQUEST, message);
        }
    }
}
