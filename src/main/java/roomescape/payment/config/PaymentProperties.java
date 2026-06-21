package roomescape.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    private long reservationAmount = 1_000L;
    private Toss toss = new Toss();

    public long reservationAmount() {
        return reservationAmount;
    }

    public void setReservationAmount(long reservationAmount) {
        this.reservationAmount = reservationAmount;
    }

    public Toss toss() {
        return toss;
    }

    public void setToss(Toss toss) {
        this.toss = toss;
    }

    public static class Toss {
        private String baseUrl = "https://api.tosspayments.com";
        private String clientKey = "";
        private String secretKey = "";
        private Duration connectTimeout = Duration.ofSeconds(1);
        private Duration readTimeout = Duration.ofSeconds(2);

        public String baseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String clientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String secretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public Duration connectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration readTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
}
