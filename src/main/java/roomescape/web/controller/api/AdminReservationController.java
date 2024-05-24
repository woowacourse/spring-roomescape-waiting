package roomescape.web.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationAndWaitingService;
import roomescape.service.ReservationService;
import roomescape.service.request.AdminSearchedReservationDto;
import roomescape.service.request.ReservationSaveDto;
import roomescape.service.response.ReservationDto;
import roomescape.web.controller.request.AdminReservationRequest;
import roomescape.web.controller.response.AdminReservationResponse;
import roomescape.web.controller.response.MemberReservationResponse;

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
    public ResponseEntity<AdminReservationResponse> reserve(
            @Valid @RequestBody AdminReservationRequest request) {
        ReservationSaveDto appRequest = new ReservationSaveDto(request.date(), request.timeId(),
                request.themeId(), request.memberId());

        ReservationDto appResponse = reservationService.save(appRequest);
        AdminReservationResponse adminReservationResponse = new AdminReservationResponse(
                appResponse.date().getDate(),
                appRequest.timeId(), appRequest.themeId(), appRequest.memberId());

        return ResponseEntity.created(URI.create("/reservations/" + appResponse.id()))
                .body(adminReservationResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberReservationResponse>> getSearchedReservations(
            @RequestParam(required = false) @Positive Long memberId,
            @RequestParam(required = false) @Positive Long themeId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        AdminSearchedReservationDto appRequest = new AdminSearchedReservationDto(
                memberId, themeId, dateFrom, dateTo);

        List<ReservationDto> appResponses = reservationService.findAllSearched(appRequest);

        List<MemberReservationResponse> webResponse = appResponses.stream()
                .map(MemberReservationResponse::from)
                .toList();

        return ResponseEntity.ok().body(webResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationAndWaitingService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
