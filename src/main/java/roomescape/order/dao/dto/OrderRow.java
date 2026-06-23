package roomescape.order.dao.dto;

public record OrderRow(Long id, Long reservationId, String orderId, Long amount) {
}
