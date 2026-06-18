package roomescape.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;

import java.time.LocalTime;


/***
 * 양방향 매핑 테스트 컨트롤러
 *
 * 1단계 이후 삭제할 예정
 */
@RestController
public class TestController {

    private final TestService service;

    public TestController(TestService service) {
        this.service = service;
    }

    @GetMapping("/test1")
    public ResponseEntity<Slot> getTest1() {
        return ResponseEntity.ok(service.test1());
    }

    @GetMapping("/test2")
    public ResponseEntity<LocalTime> getTest2() {
        return ResponseEntity.ok(service.test2());
    }
}
