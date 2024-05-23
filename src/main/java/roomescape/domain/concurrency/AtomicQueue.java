package roomescape.domain.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public interface AtomicQueue<K, V> {

    boolean ifFirstRunElseWait(V value, K key, BiConsumer<V, K> callback);

    void add(V value, K key);

    AtomicBoolean getLock(K key);

    boolean isFirst(V value, K key);

    void sleepWhileLock(AtomicBoolean lock, long time);
}
