package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 화면 서빙 컨트롤러.
 * /admin GET 요청을 정적 HTML 파일로 포워딩합니다.
 */
@Controller
@RequestMapping("/page")
public class AdminPageController {

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/admin.html";
    }
}
