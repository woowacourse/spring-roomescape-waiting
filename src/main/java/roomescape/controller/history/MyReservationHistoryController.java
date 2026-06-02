package roomescape.controller.history;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.history.dto.MyReservationHistoryResponse;
import roomescape.service.history.MyReservationHistoryService;

@RestController
@RequestMapping("/my-histories")
public class MyReservationHistoryController {

    private final MyReservationHistoryService myReservationHistoryService;

    public MyReservationHistoryController(final MyReservationHistoryService myReservationHistoryService) {
        this.myReservationHistoryService = myReservationHistoryService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<MyReservationHistoryResponse>> getMyHistories(@PathVariable final String name) {
        return ResponseEntity.ok(myReservationHistoryService.getAllByName(name).stream()
                .map(MyReservationHistoryResponse::from)
                .toList());
    }
}
