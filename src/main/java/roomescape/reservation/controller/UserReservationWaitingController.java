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
import roomescape.reservation.application.UserReservationWaitingService;
import roomescape.reservation.controller.dto.request.CreateReservationWaitingUserRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations/waiting")
public class UserReservationWaitingController {

    private final UserReservationWaitingService userReservationWaitingService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(
            @RequestBody @Valid CreateReservationWaitingUserRequest request,
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        userReservationWaitingService.create(request.toServiceRequest(member.id()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthenticatedMember member
    ) {
        userReservationWaitingService.delete(id, member.id());
    }
}
