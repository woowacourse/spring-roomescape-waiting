package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.resolver.AuthenticationPrincipal;
import roomescape.reservation.application.ReservationWaitingService;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;
import roomescape.reservation.controller.dto.request.CreateWaitingRequest;
import roomescape.reservation.controller.dto.response.WaitingResponse;

@RestController
@RequestMapping("/reservations-wait")
@RequiredArgsConstructor
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public WaitingResponse create(
        @RequestBody @Valid CreateWaitingRequest request,
        @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        Long memberId = authenticatedMember.id();
        WaitingServiceResponse response = reservationWaitingService.create(
            request.toServiceRequest(memberId));
        return WaitingResponse.from(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
         reservationWaitingService.deleteById(id);
    }
}
