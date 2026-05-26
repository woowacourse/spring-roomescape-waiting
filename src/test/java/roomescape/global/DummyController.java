package roomescape.global;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.DuplicateEntityException;
import roomescape.domain.EntityNotFoundException;

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
    public ResponseEntity<Long> business() {
        throw new IllegalArgumentException("비즈니스 예외");
    }

    @GetMapping("/dummy/entityNotFound")
    public ResponseEntity<Long> entityNotFound() {
        throw new EntityNotFoundException("데이터 없음");
    }

    @GetMapping("/dummy/duplicateEntity")
    public ResponseEntity<Long> duplicateEntity() {
        throw new DuplicateEntityException("충돌");
    }

    @GetMapping("/dummy/dataIntegrity")
    public ResponseEntity<Long> dataIntegrity() {
        throw new DataIntegrityViolationException("무결성");
    }

    @GetMapping("/dummy/internal")
    public ResponseEntity<Long> internal() {
        throw new RuntimeException();
    }

    @GetMapping("/api/admin/test")
    public void accessCheck() {
    }

    @GetMapping("/api/client")
    public void accessClient() {
    }
}
