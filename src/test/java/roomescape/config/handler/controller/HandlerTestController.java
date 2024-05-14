package roomescape.config.handler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HandlerTestController {

    @GetMapping("/admin/handler-test")
    public void adminTestEndpoint() {
    }
}
