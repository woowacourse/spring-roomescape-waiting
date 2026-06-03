package roomescape.infra.queue;

public record AsyncMessage<T>(String jobId, T request) {
}
