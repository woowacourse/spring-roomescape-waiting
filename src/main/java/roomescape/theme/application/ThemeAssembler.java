package roomescape.theme.application;

import org.springframework.stereotype.Component;
import roomescape.theme.Theme;

@Component
public class ThemeAssembler {

    public Theme assemble(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }
}
