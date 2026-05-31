package roomescape.dto.response;

import roomescape.domain.Store;

public record StoreResponseDto(
        Long id,
        String name
) {
    public static StoreResponseDto from(Store store) {
        return new StoreResponseDto(store.getId(), store.getName());
    }
}
