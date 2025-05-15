package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.AuthenticatedMember;
import roomescape.auth.web.resolver.AuthenticationPrincipal;
import roomescape.reservation.application.MyReservationService;
import roomescape.reservation.controller.dto.response.MyReservationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations-mine")
public class MyReservationController {

    private final MyReservationService myReservationService;

    @GetMapping
    public List<MyReservationResponse> getAll(@AuthenticationPrincipal AuthenticatedMember member) {
        return myReservationService.getAllByMemberId(member.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
