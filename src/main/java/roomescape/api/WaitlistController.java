package roomescape.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.WaitlistService;

@RestController
@RequestMapping("/waitlists")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> cancelMyWaitlist(@PathVariable Long id, @RequestParam String name) {
        waitlistService.cancelMyWaitlist(id, name);
        return ResponseEntity.noContent().build();
    }
}
