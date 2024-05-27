package roomescape.service.dto;

import java.time.LocalDate;

public class WaitingDto {

    private final LocalDate date;
    private final long timeId;
    private final long themeId;
    private final long memberId;

    public WaitingDto(LocalDate date, Long timeId, Long themeId, Long memberId) {
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.memberId = memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }

    public long getThemeId() {
        return themeId;
    }

    public long getMemberId() {
        return memberId;
    }
}
