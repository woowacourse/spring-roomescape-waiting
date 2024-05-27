package roomescape.service.dto.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import java.util.List;

public class AvailableTimeResponses {

    private final List<AvailableTimeResponse> availableTimeResponses;

    @JsonCreator(mode = Mode.PROPERTIES)
    public AvailableTimeResponses(List<AvailableTimeResponse> availableTimeResponses) {
        this.availableTimeResponses = availableTimeResponses;
    }

    public List<AvailableTimeResponse> getAvailableTimeResponses() {
        return availableTimeResponses;
    }
}
