package roomescape.dto.response;

public record PaymentConfirmResponse(boolean isConfirmed) {

    public static PaymentConfirmResponse confirmed() {
        return new PaymentConfirmResponse(true);
    }

    public static PaymentConfirmResponse uncertain() {
        return new PaymentConfirmResponse(false);
    }
}
