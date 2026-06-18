package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import roomescape.payment.PaymentConfirmation;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * [학습 실험] 진짜 connect timeout 은 SYN 무응답(블랙홀 IP)으로만 재현된다.
 * 세 케이스의 경과 시간을 측정해 /tmp/connect-experiment.txt 로 남긴다.
 *
 * <p>실측 결과(예시):
 * <pre>
 * ① 블랙홀 + connect timeout 500ms 있음   → ~530ms 후 PaymentConnectionException
 * ② 블랙홀 + connect timeout 없음(무한)    → 3000ms 지나도 매달림(OS 기본값까지 스레드 점유)
 * ③ 닫힌 포트 + connect timeout 500ms      → ~2ms 즉시 거부(timeout 재현 안 됨)
 * </pre>
 */
class ConnectTimeoutExperiment {

    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("pk", "order-1", 1000L, "idem-key");
    }

    private TossPaymentGateway gateway(String baseUrl, int connectMs, int readMs) {
        return new TossPaymentGateway(
                new TossClientConfig().tossRestClient(baseUrl, "test_gsk_dummy", connectMs, readMs),
                new ObjectMapper());
    }

    @Test
    void 세_케이스_경과시간_측정() throws Exception {
        StringBuilder out = new StringBuilder();

        out.append(measureSync("① 블랙홀 + connect timeout 500ms 있음",
                gateway(BLACKHOLE_URL, 500, 500)));

        out.append(measureNoTimeout("② 블랙홀 + connect timeout 없음(0=무한), 3초만 관찰",
                gateway(BLACKHOLE_URL, 0, 0)));

        int closedPort;
        try (ServerSocket s = new ServerSocket(0)) {
            closedPort = s.getLocalPort();
        }
        out.append(measureSync("③ 닫힌 포트(127.0.0.1:" + closedPort + ") + connect timeout 500ms",
                gateway("http://127.0.0.1:" + closedPort, 500, 500)));

        Files.writeString(Path.of("/tmp/connect-experiment.txt"), out.toString());
        System.out.println(out);
    }

    private String measureSync(String label, TossPaymentGateway gateway) {
        long start = System.nanoTime();
        String result;
        try {
            gateway.confirm(confirmation());
            result = "예외 없이 반환(예상 밖)";
        } catch (Throwable t) {
            result = t.getClass().getSimpleName()
                    + (t.getCause() != null ? " (cause=" + t.getCause().getClass().getSimpleName() + ")" : "");
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        return "%s%n   → %d ms 후 %s%n%n".formatted(label, ms, result);
    }

    private String measureNoTimeout(String label, TossPaymentGateway gateway) throws Exception {
        ThreadFactory daemon = r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        };
        ExecutorService executor = Executors.newSingleThreadExecutor(daemon);
        long start = System.nanoTime();
        Future<?> f = executor.submit(() -> gateway.confirm(confirmation()));
        try {
            f.get(3, TimeUnit.SECONDS);
            long ms = (System.nanoTime() - start) / 1_000_000;
            return "%s%n   → %d ms 만에 끝남(예상 밖)%n%n".formatted(label, ms);
        } catch (TimeoutException e) {
            return "%s%n   → 3000 ms 가 지나도 여전히 매달려 있음(아직 실패 안 함)%n%n".formatted(label);
        } finally {
            executor.shutdownNow();
        }
    }
}
