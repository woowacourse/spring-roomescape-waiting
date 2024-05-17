package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.controller.dto.ReservationStatusRequest;

public class ReservationSaveRequest {
    @NotNull
    @Positive
    private Long memberId;

    @NotNull
    @FutureOrPresent(message = "지나간 날짜의 예약을 할 수 없습니다.")
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    @NotNull
    @Positive
    private Long timeId;

    @NotNull
    @Positive
    private Long themeId;

    @NotNull
    private ReservationStatusRequest status = ReservationStatusRequest.BOOKED;

    protected ReservationSaveRequest() {
    }

    public ReservationSaveRequest(
            Long memberId,
            LocalDate date,
            Long timeId,
            Long themeId,
            ReservationStatusRequest status
    ) {
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.status = status;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public ReservationStatusRequest getStatus() {
        return status;
    }
}

