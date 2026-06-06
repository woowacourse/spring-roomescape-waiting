package roomescape.infra.queue;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncQueue<T, R> {

    private final ConcurrentMap<String, ExecutorService> slotExecutors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, JobResult<R>> results = new ConcurrentHashMap<>();

    public String enqueue(T request) {
        String slotId = toSlotId(request);
        String jobId = toJobId(request);

        results.put(jobId, JobResult.pending());

        slotExecutors
                .computeIfAbsent(slotId, k -> Executors.newSingleThreadExecutor(Thread.ofVirtual().factory()))
                .submit(() -> results.put(jobId, process(request)));

        return jobId;
    }

    protected abstract String toSlotId(T request);

    protected abstract String toJobId(T request);

    protected abstract JobResult<R> process(T request);

    public JobResult<R> getResult(String jobId) {
        return results.get(jobId);
    }

    protected void evictBefore(LocalDate date) {
        results.keySet().removeIf(key -> isBeforeDate(key, date));
        slotExecutors.entrySet().removeIf(entry -> {
            if (isBeforeDate(entry.getKey(), date)) {
                entry.getValue().shutdown();
                return true;
            }
            return false;
        });
    }

    private boolean isBeforeDate(String key, LocalDate date) {
        try {
            return LocalDate.parse(key.split(":")[0]).isBefore(date);
        } catch (Exception e) {
            return false;
        }
    }
}
