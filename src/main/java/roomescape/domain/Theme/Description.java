package roomescape.domain.Theme;

public class Description {

    private final String description;

    public Description(String description) {
        validateDescription(description);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    private void validateDescription(String description) {
        if (description.length() < 10) {
            throw new IllegalArgumentException(
                    "[ERROR] 설명은 10글자 이상 입력해주세요.",
                    new Throwable(" theme_description : " + description)
            );
        }
    }
}
