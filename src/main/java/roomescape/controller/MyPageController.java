package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.MemberBookingResponse;
import roomescape.service.MyPageService;
import roomescape.service.WaitingService;
import roomescape.service.result.MemberBookingResult;

@RestController
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final WaitingService waitingService;

    public MyPageController(MyPageService myPageService, WaitingService waitingService) {
        this.myPageService = myPageService;
        this.waitingService = waitingService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<MemberBookingResponse>> getMyBookings(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<MemberBookingResult> results = myPageService.getMyBookings(loginMemberInfo.id());

        return ResponseEntity.ok(MemberBookingResponse.from(results));
    }

    @DeleteMapping("/waitings/{waitingId}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable("waitingId") Long waitingId,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        waitingService.cancelWaitingById(waitingId, loginMemberInfo);
        return ResponseEntity.noContent().build();
    }
}
