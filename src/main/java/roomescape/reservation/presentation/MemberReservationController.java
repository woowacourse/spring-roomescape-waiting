package roomescape.reservation.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;
import roomescape.common.exception.handler.dto.ExceptionResponse;
import roomescape.reservation.application.ReservationFacadeService;
import roomescape.reservation.waiting.application.WaitingReservationFacadeService;
import roomescape.reservation.presentation.dto.MyReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.WaitingReservationResponse;

@RestController
public class MemberReservationController {

    private final ReservationFacadeService reservationService;
    private final WaitingReservationFacadeService waitingReservationFacadeService;

    public MemberReservationController(ReservationFacadeService reservationService,
                                       WaitingReservationFacadeService waitingReservationFacadeService) {
        this.reservationService = reservationService;
        this.waitingReservationFacadeService = waitingReservationFacadeService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody final ReservationRequest request,
                                                                 @LoginMember final LoginMemberInfo memberInfo) {
        ReservationResponse response = reservationService.createReservation(request, memberInfo.id());
        return ResponseEntity.created(URI.create("/reservation")).body(response);
    }

    @PostMapping("/reservation-waiting")
    public ResponseEntity<WaitingReservationResponse> createWaitingReservation(@RequestBody final ReservationRequest request,
                                                                               @LoginMember final LoginMemberInfo memberInfo) {
        WaitingReservationResponse response = waitingReservationFacadeService.createWaitingReservation(request, memberInfo.id());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<MyReservationResponse> response = reservationService.getMemberReservations(loginMemberInfo);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @PathVariable("id") Long reservationId) {
        waitingReservationFacadeService.deleteByIdWithMemberId(loginMemberInfo.id(), reservationId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ExceptionResponse> noMatchDateType(final HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
            400, "[ERROR] 요청 날짜 형식이 맞지 않습니다.", request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}
