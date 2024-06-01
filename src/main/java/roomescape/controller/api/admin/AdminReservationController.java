package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationDto;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationDeletionService;
import roomescape.service.ReservationQueryService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationDeletionService reservationDeletionService;

    public AdminReservationController(
            ReservationCreationService reservationCreationService,
            ReservationQueryService reservationQueryService,
            ReservationDeletionService reservationDeletionService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationQueryService = reservationQueryService;
        this.reservationDeletionService = reservationDeletionService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addAdminReservation(@RequestBody AdminReservationRequest request) {
        ReservationDto reservationDto = ReservationDto.mapToApplicationDto(request);
        ReservationResponse reservationResponse = reservationCreationService.addReservation(reservationDto);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())) //todo: 헤더에 들어간다는 것을 더 가독성있게 명시하기
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<ReservationResponse>> getAllReservedReservations() {
        List<ReservationResponse> reservations = reservationQueryService.getAllReservedReservations();
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/waiting")
    public ResponseEntity<MultipleResponse<ReservationResponse>> getAllWaitingReservations() {
        List<ReservationResponse> reservations = reservationQueryService.getAllWaitingReservations();
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<MultipleResponse<ReservationResponse>> getFilteredReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResponse> reservations
                = reservationQueryService.getFilteredReservations(themeId, memberId, dateFrom, dateTo);
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Member member) {
        reservationDeletionService.deleteByAdmin(id, member);

        return ResponseEntity.noContent()
                .build();
    }
}
