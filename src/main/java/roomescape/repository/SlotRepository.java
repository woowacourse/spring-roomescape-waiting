package roomescape.repository;

import roomescape.domain.Slot;

public interface SlotRepository {

    Slot getOrCreate(Slot slot);
}
