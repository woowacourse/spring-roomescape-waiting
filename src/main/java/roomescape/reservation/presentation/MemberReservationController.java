package roomescape.reservation.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;
import roomescape.common.exception.handler.dto.ExceptionResponse;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
        @RequestBody final ReservationRequest request,
        @LoginMember final LoginMemberInfo memberInfo)
    {
        ReservationResponse response = reservationService.createReservation(request, memberInfo.id());
        return ResponseEntity.created(URI.create("/reservation")).body(response);
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
        @RequestBody final ReservationRequest request,
        @LoginMember final LoginMemberInfo memberInfo
    )
    {
        WaitingResponse response = reservationService.createWaiting(request, memberInfo.id());
        return ResponseEntity.created(URI.create("/reservations/waiting")).body(response);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") Long id) {
        reservationService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<MyReservationResponse> response = reservationService.getMemberReservations(loginMemberInfo);

        return ResponseEntity.ok().body(response);
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ExceptionResponse> noMatchDateType(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            400, "[ERROR] 요청 날짜 형식이 맞지 않습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
