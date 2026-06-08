package roomescape.reservationslot.service.support;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.service.support.FakeWaitingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private long nextId = 1L;
    private final List<ReservationSlot> slots = new ArrayList<>();
    private final FakeReservationRepository reservationRepository;
    private final FakeWaitingRepository waitingRepository;

    public FakeReservationSlotRepository(
            final FakeReservationRepository reservationRepository,
            final FakeWaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Override
    public ReservationSlot findOrCreate(final LocalDate date, final ReservationTime time, final Theme theme) {
        return findByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())
                .orElseGet(() -> {
                    ReservationSlot slot = ReservationSlot.of(nextId++, date, time, theme);
                    slots.add(slot);
                    return slot;
                });
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(
            final LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        return slots.stream()
                .filter(slot -> slot.getDate().equals(date))
                .filter(slot -> slot.getTime().getId().equals(timeId))
                .filter(slot -> slot.getTheme().getId().equals(themeId))
                .findFirst();
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeIdForUpdate(
            final LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        return findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public void deleteReservationAndPromoteWaiting(final Reservation reservation) {
        if (!reservationRepository.removeById(reservation.getId())) {
            throw new ReservationNotFoundException();
        }

        waitingRepository.findEarliestBySlotId(reservation.getSlotId())
                .ifPresent(waiting -> {
                    waitingRepository.deleteById(waiting.getId());
                    reservationRepository.save(Reservation.of(
                            null,
                            waiting.getCustomerName().name(),
                            waiting.getCustomerEmail(),
                            waiting.getSlot()
                    ));
                });
    }

    public void add(final ReservationSlot slot) {
        slots.add(slot);
        nextId = Math.max(nextId, slot.getId() + 1);
    }
}
