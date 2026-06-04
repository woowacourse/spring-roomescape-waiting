package roomescape.service.dto.command;

import org.springframework.web.multipart.MultipartFile;
import roomescape.controller.dto.request.ThemeRequest;

public record ThemeCommand(
        String name,
        String description,
        MultipartFile file
) {
    public static ThemeCommand from(ThemeRequest request) {
        return new ThemeCommand(request.name(), request.description(), request.file());
    }
}
