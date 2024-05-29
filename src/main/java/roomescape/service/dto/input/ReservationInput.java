package roomescape.service.dto.input;

public record ReservationInput(String date, Long timeId, Long themeId, Long memberId) {

    public ReservationInfoInput parseReservationInfoInput() {
        return new ReservationInfoInput(date,timeId,themeId);
    }
}
