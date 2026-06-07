package roomescape.dto.response;

import roomescape.domain.Store;

import java.util.List;

public record StoreResponse(
        Long id,
        String name
) {
    public static List<StoreResponse> fromAll(List<Store> stores) {
        return stores.stream()
                .map(StoreResponse::from)
                .toList();
    }

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName());
    }
}
