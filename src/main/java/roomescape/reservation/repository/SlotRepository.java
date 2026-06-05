package roomescape.reservation.repository;

public interface SlotRepository {

    void ensure(Long themeId, Long timeId);

    void lock(Long themeId, Long timeId);
}
