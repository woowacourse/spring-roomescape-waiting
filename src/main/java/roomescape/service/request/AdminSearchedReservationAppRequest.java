package roomescape.service.request;

public record AdminSearchedReservationAppRequest(Long memberId, Long themeId, String dateFrom, String dateTo) {
}
