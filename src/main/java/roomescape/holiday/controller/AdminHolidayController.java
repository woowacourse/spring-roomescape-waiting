package roomescape.holiday.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.holiday.controller.dto.HolidayResponseDto;
import roomescape.holiday.controller.dto.HolidaySaveRequestDto;
import roomescape.holiday.service.HolidayService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminHolidayController {

    private final HolidayService holidayService;

    public AdminHolidayController(HolidayService holidayService)
    {
        this.holidayService = holidayService;
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<HolidayResponseDto>> getAll() {
        List<HolidayResponseDto> body = holidayService.getAll().stream()
                .map(HolidayResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/holidays")
    public ResponseEntity<HolidayResponseDto> create(
            @RequestBody @Valid HolidaySaveRequestDto holidayRequest) {
        HolidayResponseDto body = HolidayResponseDto.from(
                holidayService.create(holidayRequest.toServiceDto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        holidayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
