package roomescape.core.dto.waiting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class WaitingRequest {
    @NotNull(message = "예약자 ID는 비어있을 수 없습니다.")
    private Long memberId;

    @NotBlank(message = "날짜는 비어있을 수 없습니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다.")
    private String date;

    @NotNull(message = "시간 ID는 null일 수 없습니다.")
    private Long timeId;

    @NotNull(message = "테마 ID는 null일 수 없습니다.")
    private Long themeId;

    public WaitingRequest() {
    }

    public WaitingRequest(final Long memberId, String date, final Long timeId, final Long themeId) {
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
