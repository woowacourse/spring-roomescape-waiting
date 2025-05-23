package roomescape.waiting.dto;

public record WaitingCreateResponse(
        String message
) {

    public static WaitingCreateResponse success(Long rank) {
        return new WaitingCreateResponse(rank + "번째 예약대기 되었습니다");
    }
}
