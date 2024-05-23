package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record FindTimeAndAvailabilityDto(Long id,
                                         @JsonFormat(pattern = "HH:mm") LocalTime startAt,
                                         boolean alreadyBooked) { }
