package roomescape.time.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.ReservationTime;

public record TimeResponseDto(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
) {

  public static TimeResponseDto from(ReservationTime time) {
    return new TimeResponseDto(time.getId(), time.getStartAt(), time.getEndAt());
  }
}
