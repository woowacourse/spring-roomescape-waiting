package roomescape.date.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;

public class FakeReservationDateRepository implements ReservationDateRepository {

    private final List<ReservationDate> reservationDates = new ArrayList<>();
    private Long autoIncrement = 0L;

    @Override
    public Optional<ReservationDate> findById(Long id) {
        return reservationDates.stream()
                .filter(reservationDate -> reservationDate.getId() == id)
                .findFirst();
    }

    @Override
    public List<ReservationDate> findAll() {
        return reservationDates.stream().toList();
    }

    @Override
    public List<ReservationDate> findAllAfterToday() {
        return List.of();
    }

    @Override
    public List<ReservationDate> findSlotOfDatesByThemeId(Long themeId) {
        return List.of();
    }

    @Override
    public ReservationDate save(ReservationDate reservationDate) {
        autoIncrement();
        ReservationDate savedReservationDate = ReservationDate.load(autoIncrement, reservationDate.getDate(), true);
        this.reservationDates.add(savedReservationDate);
        return savedReservationDate;
    }

    public List<ReservationDate> saveAll(List<ReservationDate> reservationDates) {
        List<ReservationDate> savedReservationDates = new ArrayList<>();
        for (ReservationDate reservationDate : reservationDates) {
            savedReservationDates.add(save(reservationDate));
        }
        return savedReservationDates;
    }

    @Override
    public boolean updateStatus(ReservationDate reservationDate) {
        boolean isActive = reservationDate.isActive();
        Optional<ReservationDate> target = findById(reservationDate.getId());
        if (target.isEmpty()) {
            return false;
        }
        target.get().updateStatus(isActive);
        return true;
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        return reservationDates.stream()
                .anyMatch(reservationDate -> reservationDate.getDate().equals(date));
    }

    private void autoIncrement() {
        autoIncrement = autoIncrement + 1;
    }

}
