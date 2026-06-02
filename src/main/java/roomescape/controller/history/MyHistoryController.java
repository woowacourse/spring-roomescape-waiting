package roomescape.controller.history;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.history.dto.MyHistoryResponse;
import roomescape.service.history.MyHistoryService;

@RestController
@RequestMapping("/my-histories")
public class MyHistoryController {

    private final MyHistoryService myHistoryService;

    public MyHistoryController(final MyHistoryService myHistoryService) {
        this.myHistoryService = myHistoryService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<MyHistoryResponse>> getHistorys(@PathVariable final String name) {
        return ResponseEntity.ok(myHistoryService.getAllByName(name).stream()
                .map(MyHistoryResponse::from)
                .toList());
    }
}
