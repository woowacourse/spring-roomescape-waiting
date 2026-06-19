package roomescape.theme.application;

import org.springframework.stereotype.Component;
import roomescape.theme.domain.Theme;

@Component
public class ThemeAssembler {

    public Theme assemble(String name, String description, String thumbnailUrl, Integer price) {
        int resolvedPrice = price == null ? Theme.DEFAULT_PRICE : price;
        return new Theme(null, name, description, thumbnailUrl, resolvedPrice);
    }
}
