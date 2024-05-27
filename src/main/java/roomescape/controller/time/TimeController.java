package roomescape.controller.time;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.time.dto.AvailabilityTimeRequest;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.controller.time.dto.ReadTimeResponse;
import roomescape.service.TimeService;

@RestController
@RequestMapping("/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(final TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public List<ReadTimeResponse> getTimes() {
        return timeService.getTimes();
    }

    @GetMapping(value = "/availability", params = {"date", "themeId"})
    public List<AvailabilityTimeResponse> getAvailableTimes(
            @Valid final AvailabilityTimeRequest availabilityTimeRequest) {
        return timeService.getAvailabilityTimes(availabilityTimeRequest);
    }
}
