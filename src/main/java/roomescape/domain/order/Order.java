package roomescape.domain.order;

public record Order(
        String id,
        Long amount,
        Long reservation_id
) {
}
