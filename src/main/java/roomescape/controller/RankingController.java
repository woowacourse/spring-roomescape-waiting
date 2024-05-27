package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.RankService;

@RestController
@RequestMapping("/ranks")
public class RankingController {

    private final RankService rankService;

    public RankingController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> findTenPopularThemes() {
        return ResponseEntity.ok(rankService.findPopularThemes());
    }
}
