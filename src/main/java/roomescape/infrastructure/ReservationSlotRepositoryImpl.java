package roomescape.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.repository.ReservationSlotRepositoryCustom;

import java.time.LocalDate;
import java.util.ArrayList;

@Repository
public class ReservationSlotRepositoryImpl implements ReservationSlotRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long save(LocalDate date, long timeId, long themeId){
        Time time = entityManager.getReference(Time.class, timeId);
        Theme theme = entityManager.getReference(Theme.class, themeId);

        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme,new ArrayList<>());
        entityManager.persist(reservationSlot);
        return reservationSlot.getId();

    }

}
