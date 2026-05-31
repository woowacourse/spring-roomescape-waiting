package roomescape.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.SendToken;
import roomescape.member.controller.dto.request.LoginDto;
import roomescape.member.controller.dto.response.TokenDto;
import roomescape.member.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @SendToken
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginDto dto) {
        String token = authService.login(dto.toCommand());
        return ResponseEntity.ok()
                .body(TokenDto.from(token));
    }

}
