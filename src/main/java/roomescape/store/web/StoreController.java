package roomescape.store.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.store.StoreService;

@RestController
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreResponseDto>> findAll() {
        List<StoreResponseDto> responses = storeService.findAll().stream()
                .map(StoreResponseDto::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
