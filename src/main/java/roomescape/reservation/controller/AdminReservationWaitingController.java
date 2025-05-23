package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.AdminReservationWaitingService;
import roomescape.reservation.application.dto.response.AdminReservationWaitingServiceResponse;
import roomescape.reservation.controller.dto.response.AdminReservationWaitingResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reservations/waiting")
public class AdminReservationWaitingController {

    private final AdminReservationWaitingService adminReservationWaitingService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<AdminReservationWaitingResponse> getAll() {
        List<AdminReservationWaitingServiceResponse> responses = adminReservationWaitingService.getAll();
        return responses.stream()
                .map(AdminReservationWaitingResponse::from)
                .toList();
    }
}
