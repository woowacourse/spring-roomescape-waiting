package roomescape.service.dto;

import java.time.LocalDate;

public record WaitingCreateDto(
      LocalDate localDate,
      long memberId,
      long themeId,
      long timeId) {
}
