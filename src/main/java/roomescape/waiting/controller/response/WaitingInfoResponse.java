package roomescape.waiting.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingInfoResponse(Long id,
                                  String name,
                                  String theme,
                                  @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  LocalTime startAt
) {
}
