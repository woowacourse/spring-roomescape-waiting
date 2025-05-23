package roomescape.reservation.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.exceptionHandler.dto.ExceptionResponse;
import roomescape.member.argumentResolver.Authentication;
import roomescape.member.dto.request.LoginMember;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationWaitingRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationController {

    public static final String RESERVATION_BASE_URL = "/reservations";
    private static final String SLASH = "/";

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(RESERVATION_BASE_URL)
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @ModelAttribute ReservationConditionRequest request) {
        List<ReservationResponse> response = reservationService.getReservations(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(RESERVATION_BASE_URL)
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final ReservationRequest request,
                                                                 @Authentication final LoginMember loginMember) {
        ReservationResponse response = reservationService.createReservation(request, loginMember.id());
        URI locationUri = URI.create(RESERVATION_BASE_URL + SLASH + response.id());
        return ResponseEntity.created(locationUri).body(response);
    }

    @PostMapping("/waiting-reservations")
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @RequestBody final ReservationWaitingRequest request,
            @Authentication final LoginMember loginMember) {
        ReservationResponse response = reservationService.createWaitingReservation(request, loginMember.id());
        URI locationUri = URI.create(RESERVATION_BASE_URL + SLASH + response.id());
        return ResponseEntity.created(locationUri).body(response);
    }

    @DeleteMapping(RESERVATION_BASE_URL + "/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable("id") final Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(RESERVATION_BASE_URL + "/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@Authentication LoginMember loginMember) {
        List<MyReservationResponse> myReservationResponses = reservationService.getMyReservations(loginMember.id());
        return ResponseEntity.ok().body(myReservationResponses);
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ExceptionResponse> noMatchDateType(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                "[ERROR] 요청 날짜 형식이 맞지 않습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
