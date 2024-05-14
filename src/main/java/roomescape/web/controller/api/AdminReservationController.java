package roomescape.web.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationService;
import roomescape.service.request.AdminSearchedReservationAppRequest;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;
import roomescape.web.controller.request.AdminReservationWebRequest;
import roomescape.web.controller.response.AdminReservationWebResponse;
import roomescape.web.controller.response.MemberReservationWebResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
        @RequestParam @NotNull @Positive Long memberId,
        @RequestParam @NotNull @Positive Long themeId,
        @RequestParam @NotBlank(message = "From 날짜는 필수입니다.") String dateFrom,
        @RequestParam @NotBlank(message = "To 날짜는 필수입니다.") String dateTo) {

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
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
