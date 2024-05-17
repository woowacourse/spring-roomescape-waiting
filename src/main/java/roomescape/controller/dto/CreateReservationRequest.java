package roomescape.controller.dto;

public record CreateReservationRequest(Long memberId, String date, Long timeId, Long themeId) {

  public CreateReservationRequest {
    validate(memberId, date, timeId, themeId);
  }

  private void validate(Long memberId, String date, Long timeId, Long themeId) {
    if (memberId == null || date == null || date.isBlank() || timeId == null || themeId == null) {
      throw new IllegalArgumentException("요청 필드는 비어있을 수 없습니다.");
    }
  }
}
