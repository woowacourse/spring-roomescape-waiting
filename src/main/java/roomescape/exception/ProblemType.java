package roomescape.exception;

import java.net.URI;

public enum ProblemType {

    NOT_FOUND("not-found", "리소스를 찾을 수 없음"),
    UNAUTHORIZED("unauthorized", "본인 확인 실패"),
    CONFLICT("conflict", "요청이 현재 상태와 충돌함"),
    CONCURRENCY_CONFLICT("concurrency-conflict", "동시 요청이 충돌함"),
    BUSINESS_RULE_VIOLATION("business-rule-violation", "비즈니스 정책 위반"),
    VALIDATION_ERROR("validation-error", "요청 본문 검증 실패"),
    BAD_REQUEST("bad-request", "잘못된 요청"),
    METHOD_NOT_SUPPORTED("method-not-supported", "지원하지 않는 HTTP 메서드"),
    MEDIA_TYPE_NOT_SUPPORTED("media-type-not-supported", "지원하지 않는 미디어 타입"),
    NOT_ACCEPTABLE("not-acceptable", "응답 가능한 미디어 타입 없음"),
    NO_RESOURCE("no-resource", "리소스를 찾을 수 없음"),
    PAYMENT_AMOUNT_MISMATCH("payment-amount-mismatch", "결제 금액 불일치"),
    PAYMENT_FAILED("payment-failed", "결제 실패"),
    PAYMENT_UNCERTAIN("payment-uncertain", "결제 결과 확인 필요"),
    OUTBOUND_RATE_LIMITED("outbound-rate-limited", "외부 호출 한도 초과"),
    INTERNAL_ERROR("internal-error", "서버 내부 오류");

    private static final String TYPE_BASE = "https://roomescape.example/problems/";

    private final String slug;
    private final String title;

    ProblemType(String slug, String title) {
        this.slug = slug;
        this.title = title;
    }

    public URI uri() {
        return URI.create(TYPE_BASE + slug);
    }

    public String slug() {
        return slug;
    }

    public String title() {
        return title;
    }
}
