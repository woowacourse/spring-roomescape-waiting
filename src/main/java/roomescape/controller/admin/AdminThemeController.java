package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.RoomThemeService;
import roomescape.service.dto.request.RoomThemeCreateRequest;
import roomescape.service.dto.response.RoomThemeResponse;

@RestController
@RequestMapping("/admin/themes")
public class AdminThemeController {

    private final RoomThemeService roomThemeService;

    public AdminThemeController(RoomThemeService roomThemeService) {
        this.roomThemeService = roomThemeService;
    }

    @PostMapping
    public ResponseEntity<RoomThemeResponse> createRoomTheme(
            @RequestBody @Valid RoomThemeCreateRequest roomThemeCreateRequest) {
        RoomThemeResponse roomThemeResponse = roomThemeService.save(roomThemeCreateRequest);
        return ResponseEntity.created(URI.create("/themes" + roomThemeResponse.id()))
                .body(roomThemeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomTheme(@PathVariable Long id) {
        roomThemeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
