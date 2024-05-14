package roomescape.domain.dto;

import java.time.LocalTime;

//TODO: 클라이언트 예약 통합 또는 적절한 네이밍 변경 필요
public record BookResponse(LocalTime startAt, Long timeId, Boolean alreadyBooked) {
}
