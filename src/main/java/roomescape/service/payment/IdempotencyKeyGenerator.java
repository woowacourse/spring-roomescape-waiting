package roomescape.service.payment;

public interface IdempotencyKeyGenerator {

    String generate();
}
