package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record FindTimeAndAvailabilityResponse(Long id,
                                              @JsonFormat(pattern = "HH:mm") LocalTime startAt,
                                              boolean alreadyBooked) { }
