
package roomescape.member.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.dto.ReservationWithStateDto;
import roomescape.user.domain.User;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.user.service.UserService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final UserService memberService;

    public MemberController(UserService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        List<UserResponseDto> userResponseDtos = memberService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDtos);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationWithStateDto>> findAllReservationsByMember(User member) {
        List<ReservationWithStateDto> reservations = memberService.findAllReservationByMember(member);
        return ResponseEntity.status(HttpStatus.OK).body(reservations);
    }

    @DeleteMapping("/reservations-mine/{id}")
    public ResponseEntity<Void> deleteReservationByMember(@PathVariable(value = "id") Long waitingId, User member) {
        memberService.deleteWaitingByMember(waitingId, member);
        return ResponseEntity.noContent().build();
    }
}
