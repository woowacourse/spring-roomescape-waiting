package roomescape.domain.waiting.dto;

public record WaitingResult(String status, WaitingResponse waiting, String errorMessage) {

    public static WaitingResult pending() {
        return new WaitingResult("PENDING", null, null);
    }

    public static WaitingResult success(WaitingResponse waiting) {
        return new WaitingResult("SUCCESS", waiting, null);
    }

    public static WaitingResult failed(String errorMessage) {
        return new WaitingResult("FAILED", null, errorMessage);
    }
}