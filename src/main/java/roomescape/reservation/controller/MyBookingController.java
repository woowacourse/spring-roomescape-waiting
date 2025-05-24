package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.resolver.AuthenticationPrincipal;
import roomescape.reservation.application.MyBookingService;
import roomescape.reservation.controller.dto.response.MyReservationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations-mine")
public class MyBookingController {

    private final MyBookingService myBookingService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<MyReservationResponse> getAll(@AuthenticationPrincipal AuthenticatedMember member) {
        return myBookingService.getAllByMemberId(member.id())
            .stream()
            .map(MyReservationResponse::from)
            .toList();
    }
}
