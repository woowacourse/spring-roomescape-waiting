package roomescape.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
    }
}
