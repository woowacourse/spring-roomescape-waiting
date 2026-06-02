package roomescape.domain.waiting;

import roomescape.domain.waiting.dto.WaitingRequest;

public record WaitingMessage(String jobId, WaitingRequest request) {
}