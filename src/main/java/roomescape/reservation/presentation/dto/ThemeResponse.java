package roomescape.reservation.presentation.dto;

import org.slf4j.LoggerFactory;
import roomescape.reservation.domain.Theme;

public class ThemeResponse {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ThemeResponse.class);
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    private ThemeResponse() {
    }

    public ThemeResponse(Theme theme) {
        log.info("ididididididididididid : " + theme.getId().toString());
        this.id = theme.getId();
        this.name = theme.getName();
        this.description = theme.getDescription();
        this.thumbnail = theme.getThumbnail();

        log.info(String.valueOf(this.id));
        log.info(String.valueOf(this.name));
        log.info(String.valueOf(this.description));
        log.info(String.valueOf(this.thumbnail));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
