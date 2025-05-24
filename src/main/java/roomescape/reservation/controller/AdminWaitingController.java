package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.MyBookingService;
import roomescape.reservation.controller.dto.response.MyWaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
@RequiredArgsConstructor
public class AdminWaitingController {

    private final MyBookingService myBookingService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<MyWaitingResponse> getAll() {
        return myBookingService.getAllWaitings()
            .stream()
            .map(MyWaitingResponse::from)
            .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        myBookingService.deleteById(id);
    }
}
