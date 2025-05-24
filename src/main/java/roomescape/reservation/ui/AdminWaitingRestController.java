package roomescape.reservation.ui;


import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.AdminWaitingService;
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.WaitingResponse;
import roomescape.reservation.ui.dto.response.WaitingWithRankResponse;

@RestController
@RequestMapping("/admin/waitings")
@RequiresRole(authRoles = {ADMIN})
@RequiredArgsConstructor
public class AdminWaitingRestController {

    private final AdminWaitingService adminWaitingService;

    @PostMapping
    public ResponseEntity<WaitingResponse> create(
            @RequestBody @Valid CreateWaitingRequest request
    ) {
        final WaitingResponse response = adminWaitingService.create(request);

        return ResponseEntity.created(URI.create("/admin/waitings/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deny(
            @PathVariable final Long id
    ) {
        adminWaitingService.deleteAsAdmin(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WaitingWithRankResponse>> findAllWaitingWithRank() {
        final List<WaitingWithRankResponse> responses = adminWaitingService.findAllWaitingWithRank();

        return ResponseEntity.ok(responses);
    }
}
