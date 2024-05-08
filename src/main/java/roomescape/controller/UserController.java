package roomescape.controller;

import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import roomescape.annotation.AuthenticationPrincipal;
import roomescape.controller.request.UserLoginRequest;
import roomescape.controller.response.UserNameResponse;
import roomescape.model.User;
import roomescape.service.AuthService;
import roomescape.service.UserService;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        User user = userService.findUserByEmailAndPassword(request);
        Cookie cookie = authService.createCookieByUser(user);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<UserNameResponse> login(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserNameResponse(user.getName()));
    }

    @GetMapping("/members")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
}
