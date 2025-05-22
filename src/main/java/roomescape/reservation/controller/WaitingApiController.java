package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
@RequestMapping("/waiting")
public class WaitingApiController {

    private final WaitingService waitingService;

    public WaitingApiController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@LoginMember MemberResponse memberResponse,
                                                  @RequestBody @Valid WaitingCreateRequest request) {
        WaitingResponse response = waitingService.create(memberResponse, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        waitingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
