package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.controller.dto.request.CreateBookingRequest;
import roomescape.controller.dto.request.LoginMemberInfo;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.result.WaitingResult;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createWaiting(
            @Valid @RequestBody CreateBookingRequest createBookingRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        WaitingResult waitingResult = waitingService.create(
                createBookingRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(waitingResult));
    }
}
