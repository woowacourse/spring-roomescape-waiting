package roomescape.reservationslot.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.domain.repository.ReservationSlotRepository;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

@Repository
public class JpaReservationSlotRepositoryAdapter implements ReservationSlotRepository {

    private final JpaReservationSlotRepository jpaReservationSlotRepository;

    public JpaReservationSlotRepositoryAdapter(final JpaReservationSlotRepository jpaReservationSlotRepository) {
        this.jpaReservationSlotRepository = jpaReservationSlotRepository;
    }

    @Override
    public List<ReservationSlot> findByThemeIdAndDateBetweenAndReservationMemberId(final Long themeId,
                                                                                   final LocalDate startDate,
                                                                                   final LocalDate endDate,
                                                                                   final Long memberId) {
        return jpaReservationSlotRepository.findByThemeIdAndDateBetweenAndReservationMemberId(themeId, startDate, endDate,
                memberId);
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        return jpaReservationSlotRepository.existsByTimeId(id);
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        return jpaReservationSlotRepository.existsByThemeId(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return jpaReservationSlotRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId,
                                                                   final Long themeId) {
        return jpaReservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return jpaReservationSlotRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    @Override
    public List<ReservationSlot> findAll() {
        return jpaReservationSlotRepository.findAll();
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationSlotRepository.deleteById(id);
    }

    @Override
    public ReservationSlot save(final ReservationSlot reservationSlot) {
        return jpaReservationSlotRepository.save(reservationSlot);
    }
}
