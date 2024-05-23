package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.RoomThemeService;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.RoomThemeResponse;

@RestController
@RequestMapping("/themes")
public class RoomThemeController {
    private final RoomThemeService roomThemeService;

    public RoomThemeController(RoomThemeService roomThemeService) {
        this.roomThemeService = roomThemeService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<RoomThemeResponse>> findAllRoomThemes() {
        return ResponseEntity.ok(roomThemeService.findAll());
    }

    @GetMapping("/ranking")
    public ResponseEntity<ListResponse<RoomThemeResponse>> findAllRoomThemesRanking() {
        return ResponseEntity.ok(roomThemeService.findByRanking());
    }
}
