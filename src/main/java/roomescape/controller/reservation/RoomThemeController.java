package roomescape.controller.reservation;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.RoomThemeService;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@RestController
public class RoomThemeController {

    private final RoomThemeService roomThemeService;

    public RoomThemeController(RoomThemeService roomThemeService) {
        this.roomThemeService = roomThemeService;
    }

    @GetMapping("/themes")
    public ResponseEntity<List<RoomThemeResponse>> findAll() {
        return ResponseEntity.ok(roomThemeService.findAll());
    }

    @GetMapping("/themes/ranking")
    public ResponseEntity<List<RoomThemeResponse>> findAllRoomThemesRanking() {
        return ResponseEntity.ok(roomThemeService.findByRanking());
    }

    @PostMapping("/themes")
    public ResponseEntity<RoomThemeResponse> create(
            @RequestBody RoomThemeCreateRequest roomThemeCreateRequest)
    {
        RoomThemeResponse roomThemeResponse = roomThemeService.save(roomThemeCreateRequest);
        return ResponseEntity.created(URI.create("/themes" + roomThemeResponse.id()))
                .body(roomThemeResponse);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomThemeService.deleteById(id);
        return ResponseEntity.notFound().build();
    }
}
