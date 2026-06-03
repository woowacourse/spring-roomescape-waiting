package roomescape.infra.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AsyncQueue<T, R> {

    private final BlockingQueue<AsyncMessage<T>> queue = new LinkedBlockingQueue<>();
    private final ConcurrentMap<String, JobResult<R>> results = new ConcurrentHashMap<>();

    public String enqueue(T request) {
        String jobId = toJobId(request);
        results.put(jobId, JobResult.pending());
        queue.add(new AsyncMessage<>(jobId, request));
        return jobId;
    }

    protected abstract String toJobId(T request);

    public AsyncMessage<T> take() throws InterruptedException {
        return queue.take();
    }

    public void storeResult(String jobId, JobResult<R> result) {
        results.put(jobId, result);
    }

    public JobResult<R> getResult(String jobId) {
        return results.get(jobId);
    }
}
