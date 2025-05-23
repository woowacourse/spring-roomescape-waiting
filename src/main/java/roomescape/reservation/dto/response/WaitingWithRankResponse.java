package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Waiting;

public class WaitingWithRankResponse {

    private final Long id;
    private final String memberName;
    private final LocalDate date;
    private final TimeSlotResponse time;
    private final String themeName;
    private final Long rank;

    public WaitingWithRankResponse(Waiting waiting, Long rank) {
        this.id = waiting.getId();
        this.memberName = waiting.getMember().getName();
        this.date = waiting.getDate();
        this.time = TimeSlotResponse.from(waiting.getTimeSlot());
        this.themeName = waiting.getTheme().getName();
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlotResponse getTime() {
        return time;
    }

    public String getThemeName() {
        return themeName;
    }

    public Long getRank() {
        return rank;
    }
}
