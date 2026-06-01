package roomescape.global.error.dto;

import java.util.List;

public record ParameterErrorResponsesDto(
    String message,
    List<ParameterErrorResponseDto> parameterErrors
) {

}
