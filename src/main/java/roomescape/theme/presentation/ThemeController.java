package roomescape.theme.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.global.auth.Admin;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.presentation.dto.request.ThemeCreateRequest;
import roomescape.theme.presentation.dto.response.ThemeResponse;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService service;

    @Admin
    @PostMapping
    public ResponseEntity<ThemeResponse> saveTheme(
            @RequestBody @Valid ThemeCreateRequest request
    ) {
        ThemeCreateCommand createCommand = new ThemeCreateCommand(
                request.name(),
                request.description(),
                request.thumbnail()
        );
        ThemeResponse result = ThemeResponse.from(service.save(createCommand));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> response = (service.findAll()).stream()
                .map(ThemeResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping(params = {"sortBy", "from", "to", "limit"})
    public ResponseEntity<List<ThemeResponse>> getTopNByPeriod(
            @RequestParam String sortBy,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(required = false, defaultValue = "10") Long limit
    ) {
        List<ThemeResponse> response = (service.findTopNByPeriod(from, to, sortBy, limit)).stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable Long id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
