package roomescape.dto.waiting;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record MemberWaitingCreateRequestDto(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate localDate,
        long themeId,
        long timeId) {
}
