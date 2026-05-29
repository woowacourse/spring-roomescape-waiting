package roomescape.history.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.history.controller.dto.HistoryResponse;
import roomescape.history.service.MyHistoryService;

@RestController
@RequestMapping("/historys")
public class MyHistoryController {

    private final MyHistoryService myHistoryService;

    public MyHistoryController(final MyHistoryService myHistoryService) {
        this.myHistoryService = myHistoryService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<HistoryResponse>> getHistorys(@PathVariable final String name) {
        return ResponseEntity.ok(myHistoryService.getAllByName(name));
    }
}
