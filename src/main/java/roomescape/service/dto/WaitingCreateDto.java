package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record WaitingCreateDto(
      LocalDate localDate,
      long memberId,
      long themeId,
      long timeId) {
}
