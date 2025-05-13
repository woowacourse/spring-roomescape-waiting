package roomescape.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

public record MemberRequest(
        @Email String
        email,

        @NotNull @NumberFormat(style = Style.NUMBER)
        String password,

        @NotNull
        String name
) {
}
