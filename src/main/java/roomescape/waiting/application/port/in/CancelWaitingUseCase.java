package roomescape.waiting.application.port.in;

public interface CancelWaitingUseCase {
    void deleteByIdForUser(long waitingId, long memberId);
}
