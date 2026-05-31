package roomescape.common.advice;

import jakarta.validation.constraints.NotNull;

record DummyDto(
        @NotNull(message = "필드 not null 검증")
        String testField
) {
}
