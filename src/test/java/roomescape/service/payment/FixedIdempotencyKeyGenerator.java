package roomescape.service.payment;

public class FixedIdempotencyKeyGenerator implements IdempotencyKeyGenerator {

    private final String idempotencyKey;

    public FixedIdempotencyKeyGenerator(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String generate() {
        return idempotencyKey;
    }
}
