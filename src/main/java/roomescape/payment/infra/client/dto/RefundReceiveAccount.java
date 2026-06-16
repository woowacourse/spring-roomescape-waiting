package roomescape.payment.infra.client.dto;

public record RefundReceiveAccount(
        String bank,
        String accountNumber,
        String holderName // 환불받을 계좌의 예금
) {
}
