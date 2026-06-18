package roomescape.waiting.application.port.in;

public interface CancelWaitingUseCase {
    void deleteById(long waitingId);

    void deleteByIdForUser(long waitingId, long memberId);
}
