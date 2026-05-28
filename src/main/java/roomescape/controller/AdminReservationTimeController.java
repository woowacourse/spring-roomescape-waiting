package roomescape.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import roomescape.dto.TimeRequest;
import roomescape.dto.TimeResponse;
import roomescape.service.AdminReservationTimeService;

@RequestMapping("/admin/times")
@RestController
@Validated
public class AdminReservationTimeController {

    private final AdminReservationTimeService adminReservationTimeService;

    public AdminReservationTimeController(AdminReservationTimeService adminReservationTimeService) {
        this.adminReservationTimeService = adminReservationTimeService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<TimeResponse> getTimes() {
        return adminReservationTimeService.findAll();
    }

    @PostMapping
    public ResponseEntity<TimeResponse> createTime(@Valid @RequestBody TimeRequest request) {
        TimeResponse response = adminReservationTimeService.save(request);
        URI location = URI.create("/times/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteTime(@PathVariable Long id) {
        adminReservationTimeService.delete(id);
    }
}
