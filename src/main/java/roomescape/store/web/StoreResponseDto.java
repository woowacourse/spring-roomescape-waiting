package roomescape.store.web;

import roomescape.store.Store;

public record StoreResponseDto(
        Long id,
        String name
) {
    public static StoreResponseDto from(Store store) {
        return new StoreResponseDto(store.getId(), store.getName());
    }
}
