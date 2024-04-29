package roomescape.controller.dto;

public record CreateReservationRequest(Long memberId, String date, Long timeId, Long themeId) { }
