package roomescape.controller.history;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.history.dto.HistoryResponse;
import roomescape.service.history.MyHistoryService;

@RestController
@RequestMapping("/historys")
public class MyHistoryController {

    private final MyHistoryService myHistoryService;

    public MyHistoryController(final MyHistoryService myHistoryService) {
        this.myHistoryService = myHistoryService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<HistoryResponse>> getHistorys(@PathVariable final String name) {
        List<HistoryResponse> histories = myHistoryService.getAllByName(name).stream()
                .map(HistoryResponse::from)
                .toList();

        return ResponseEntity.ok(histories);
    }
}
