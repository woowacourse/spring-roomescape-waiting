package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Store;
import roomescape.dto.response.StoreResponse;
import roomescape.service.StoreService;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getStores() {
        List<Store> stores = storeService.getStores();
        return ResponseEntity.ok().body(StoreResponse.fromAll(stores));
    }
}
