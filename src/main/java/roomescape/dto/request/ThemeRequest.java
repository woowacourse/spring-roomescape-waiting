package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ThemeRequest(
        @NotNull @NotBlank String name,
        @NotNull String description,
        @NotNull MultipartFile file) {
}
