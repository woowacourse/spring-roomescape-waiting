package roomescape.manager;

public record Manager(
        Long id,
        Long memberId,
        Long storeId
) {
    public boolean isAccessible(long targetStoreId) {
        return storeId != null && storeId == targetStoreId;
    }
}
