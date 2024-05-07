package roomescape.controller.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AdminReservationRequest {

    @NotNull(message = "예약 날짜는 null일 수 없습니다.")
    @FutureOrPresent(message = "예약 날짜는 과거일 수 없습니다.")
    private final LocalDate date;
    @NotNull(message = "시간 아이디는 null일 수 없습니다.")
    @Min(value = 1, message = "시간 아이디는 1 이상이어야 합니다.")
    private final Long timeId;
    @NotNull(message = "테마 아이디는 null일 수 없습니다.")
    @Min(value = 1, message = "시간 아이디는 1 이상이어야 합니다.")
    private final Long themeId;
    @NotNull(message = "사용자 아이디는 null일 수 없습니다.")
    @Min(value = 1, message = "사용자 아이디는 1 이상이어야 합니다.")
    private final Long memberId;

    public AdminReservationRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.memberId = memberId;
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

    public Long getMemberId() {
        return memberId;
    }
}
