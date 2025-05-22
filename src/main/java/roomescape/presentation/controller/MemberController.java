package roomescape.presentation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.MemberService;
import roomescape.dto.response.MemberResponseDto;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MemberResponseDto> findAll() {
        return memberService.findAll();
    }
}
