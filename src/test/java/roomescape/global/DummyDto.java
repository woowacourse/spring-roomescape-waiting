package roomescape.global;

import jakarta.validation.constraints.NotNull;

public class DummyDto {

    public record DummyData(
            @NotNull(message = "필드 낫널 검증")
            Long testField
    ) {
    }
}
