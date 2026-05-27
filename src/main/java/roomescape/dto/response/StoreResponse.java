package roomescape.dto.response;

import java.util.List;
import roomescape.domain.Store;

public record StoreResponse(
        Long id,
        String name
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName());
    }

    public static List<StoreResponse> fromAll(List<Store> stores) {
        return stores.stream()
                .map(StoreResponse::from)
                .toList();
    }
}
