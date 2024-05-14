package roomescape.domain.dto;

import java.util.List;

public class TimeSlotResponses {
    private final List<TimeSlotResponse> data;

    public TimeSlotResponses(final List<TimeSlotResponse> data) {
        this.data = data;
    }

    public List<TimeSlotResponse> getData() {
        return data;
    }
}
