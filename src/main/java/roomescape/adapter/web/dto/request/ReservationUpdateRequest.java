package roomescape.adapter.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.application.dto.command.ReservationUpdateCommand;

public class ReservationUpdateRequest {
    @NotBlank(message = "예약자 이름은 비어 있을 수 없습니다.")
    private String name;

    @NotNull(message = "예약 날짜는 비어 있을 수 없습니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "예약 시간을 선택해 주세요.")
    private Long timeId;

    public ReservationUpdateRequest() {
    }

    public ReservationUpdateCommand toCommand(Long id) {
        return new ReservationUpdateCommand(id, name, date, timeId);
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }
}
