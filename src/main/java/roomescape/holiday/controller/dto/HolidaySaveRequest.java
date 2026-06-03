package roomescape.holiday.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.holiday.service.dto.HolidaySaveServiceRequest;

import java.time.LocalDate;

public record HolidaySaveRequest(Long id, @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    public HolidaySaveServiceRequest toServiceDto() {
        return new HolidaySaveServiceRequest(date);
    }
}
