package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 내 예약 화면 서빙 컨트롤러.
 */
@Controller
@RequestMapping("/page")
public class MyReservationPageController {

    @GetMapping("/my-reservations")
    public String myReservationPage() {
        return "forward:/my-reservations.html";
    }
}
