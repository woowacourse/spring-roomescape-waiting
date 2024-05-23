package roomescape.dto.waiting;

public record WaitingWebRequest(Long reservationId) {

    public WaitingRequest toServiceRequest(Long memberId) {
        return new WaitingRequest(memberId, reservationId);
    }
}
