package payment;

/**
 * 결제 승인을 외부 결제 시스템에 위임하는 포트(Port). 도메인 계층은 이 인터페이스만 알고, 어떤 PG사를 쓰는지 모른다.
 */
public interface PaymentGateway {

  PaymentResult confirm(PaymentConfirmation confirmation);

}
