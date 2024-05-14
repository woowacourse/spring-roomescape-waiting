package roomescape.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.RoomThemeService;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@RestController
@RequestMapping("/themes")
public class RoomThemeController {
    private final RoomThemeService roomThemeService;

    public RoomThemeController(RoomThemeService roomThemeService) {
        this.roomThemeService = roomThemeService;
    }

    @GetMapping
    public ResponseEntity<List<RoomThemeResponse>> findAllRoomThemes() {
        return ResponseEntity.ok(roomThemeService.findAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<RoomThemeResponse>> findAllRoomThemesRanking() {
        return ResponseEntity.ok(roomThemeService.findByRanking());
    }

    @PostMapping
    public ResponseEntity<RoomThemeResponse> createRoomTheme(
            @RequestBody RoomThemeCreateRequest roomThemeCreateRequest) {
        RoomThemeResponse roomThemeResponse = roomThemeService.save(roomThemeCreateRequest);
        return ResponseEntity.created(URI.create("/themes" + roomThemeResponse.id()))
                .body(roomThemeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomTheme(@PathVariable Long id) {
        roomThemeService.deleteById(id);
        return ResponseEntity.notFound().build();
    }
}
