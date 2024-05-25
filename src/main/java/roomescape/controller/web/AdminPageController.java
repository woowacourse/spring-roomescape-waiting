package roomescape.controller.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    private final String waitingApprovalStrategy;

    public AdminPageController(@Value("${waiting.approval-strategy}") String waitingApprovalStrategy) {
        this.waitingApprovalStrategy = waitingApprovalStrategy;
    }

    @GetMapping
    public String adminPage() {
        return "admin/index";
    }

    @GetMapping("/reservation")
    public String adminReservationPage() {
        return "admin/reservation-new";
    }

    @GetMapping("/time")
    public String adminTimePage() {
        return "admin/time";
    }

    @GetMapping("/theme")
    public String adminThemePage() {
        return "admin/theme";
    }

    @GetMapping("/waiting")
    public String adminWaitingPage(Model model) {
        model.addAttribute("approvalStrategy", waitingApprovalStrategy);
        return "admin/waiting";
    }
}
