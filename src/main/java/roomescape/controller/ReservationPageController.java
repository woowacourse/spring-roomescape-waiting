package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 사용자 예약 화면 서빙 컨트롤러.
 * /reservations GET 요청을 정적 HTML 파일로 포워딩합니다.
 */
@Controller
@RequestMapping("/page")
public class ReservationPageController {

    @GetMapping("/reservations")
    public String reservationPage() {
        return "forward:/reservations.html";
    }
}
