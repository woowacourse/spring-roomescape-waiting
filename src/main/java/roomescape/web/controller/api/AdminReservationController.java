package roomescape.web.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationAndWaitingService;
import roomescape.service.ReservationService;
import roomescape.service.request.AdminSearchedReservationAppRequest;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;
import roomescape.web.controller.request.AdminReservationWebRequest;
import roomescape.web.controller.response.AdminReservationWebResponse;
import roomescape.web.controller.response.MemberReservationWebResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final ReservationAndWaitingService reservationAndWaitingService;

    public AdminReservationController(ReservationService reservationService, ReservationAndWaitingService reservationAndWaitingService) {
        this.reservationService = reservationService;
        this.reservationAndWaitingService = reservationAndWaitingService;
    }

    @PostMapping
    public ResponseEntity<AdminReservationWebResponse> reserve(
            @Valid @RequestBody AdminReservationWebRequest request) {
        ReservationAppRequest appRequest = new ReservationAppRequest(request.date(), request.timeId(),
                request.themeId(), request.memberId());

        ReservationAppResponse appResponse = reservationService.save(appRequest);
        AdminReservationWebResponse adminReservationWebResponse = new AdminReservationWebResponse(
                appResponse.date().getDate(),
                appRequest.timeId(), appRequest.themeId(), appRequest.memberId());

        return ResponseEntity.created(URI.create("/reservations/" + appResponse.id()))
                .body(adminReservationWebResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberReservationWebResponse>> getSearchedReservations(
            @RequestParam(required = false) @Positive Long memberId,
            @RequestParam(required = false) @Positive Long themeId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        AdminSearchedReservationAppRequest appRequest = new AdminSearchedReservationAppRequest(
                memberId, themeId, dateFrom, dateTo);

        List<ReservationAppResponse> appResponses = reservationService.findAllSearched(appRequest);

        List<MemberReservationWebResponse> webResponse = appResponses.stream()
                .map(MemberReservationWebResponse::from)
                .toList();

        return ResponseEntity.ok().body(webResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationAndWaitingService.deleteIfNoWaitingOrUpdateReservation(id);

        return ResponseEntity.noContent().build();
    }
}
