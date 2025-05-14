package roomescape.theme.controller;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.service.ThemeServiceFacade;

@RestController
@RequestMapping("/themes")
public class ThemeController {
    private final ThemeServiceFacade themeService;

    @Autowired
    public ThemeController(ThemeServiceFacade themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeCreateRequest themeCreateRequest) {
        ThemeResponse response = themeService.createTheme(themeCreateRequest);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> read() {
        List<ThemeResponse> themes = themeService.findAll();
        return ResponseEntity.ok(themes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        themeService.deleteThemeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lists")
    public ResponseEntity<List<ThemeResponse>> readLists(
        @RequestParam(value = "order_type", required = false) String orderType,
        @RequestParam(value = "list_num", required = false) Long listNum
    ) {
        orderType = "popular_desc";
        listNum = 10L;
        List<ThemeResponse> listedTheme = themeService.findLimitedThemesByPopularDesc(orderType, listNum);
        return ResponseEntity.ok(listedTheme);
    }
}
