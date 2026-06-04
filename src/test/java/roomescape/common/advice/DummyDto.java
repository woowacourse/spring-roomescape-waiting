package roomescape.common.advice;

import jakarta.validation.constraints.NotNull;

public class DummyDto {

    public record DummyData(
            @NotNull(message = "필드 not null 검증")
            Long testField
    ) {
    }
}
