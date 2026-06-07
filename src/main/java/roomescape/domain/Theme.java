package roomescape.domain;

import roomescape.util.Validator;

import java.net.URI;
import java.net.URISyntaxException;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String imgUrl;

    public Theme(Long id, String name, String description, String imgUrl) {
        validateName(name);
        validateDescription(description);
        validateImgUrl(imgUrl);

        this.id = id;
        this.name = name;
        this.description = description;
        this.imgUrl = imgUrl;
    }

    private void validateName(String name) {
        Validator.notBlank(name, "이름은 비어 있을 수 없습니다.");
    }

    private void validateDescription(String description) {
        Validator.notBlank(description, "설명은 비어 있을 수 없습니다.");
    }

    private void validateImgUrl(String imgUrl) {
        Validator.notBlank(imgUrl, "URL은 비어 있을 수 없습니다.");
        try {
            URI uri = new URI(imgUrl);
            String scheme = uri.getScheme();

            if (uri.getHost() == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("올바른 URL 형식이 아닙니다.");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("올바른 URL 형식이 아닙니다.");
        }
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

    public String getImgUrl() {
        return imgUrl;
    }
}
