package roomescape.member.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
