package roomescape.controller.dto;

public record CreateThemeRequest(String name, String description, String thumbnail) {

  public CreateThemeRequest {
    validate(name, description, thumbnail);
  }

  private void validate(String name, String description, String thumbnail) {
    if (name.isBlank() || description.isBlank() || thumbnail.isBlank()) {
      throw new IllegalArgumentException("요청 필드는 비어있을 수 없습니다.");
    }
  }
}
