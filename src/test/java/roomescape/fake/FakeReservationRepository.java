package roomescape.fake;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationDetail;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class FakeReservationRepository implements ReservationRepository {

    private final FakeThemeRepository themeRepository;
    private final FakeReservationTimeRepository timeRepository;
    private final Map<Long, Reservation> reservations = new LinkedHashMap<>();
    private Long idHolder = 1L;

    public FakeReservationRepository(FakeThemeRepository themeRepository,
                                     FakeReservationTimeRepository timeRepository) {
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
    }

    @Override
    public List<ReservationDetail> findAll() {
        return List.of();
    }

    @Override
    public List<Reservation> findByName(String name) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public Optional<ReservationDetail> findDetailById(Long id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) {
            return Optional.empty();
        }

        Theme theme = themeRepository.findById(reservation.getThemeId())
                .orElseThrow();
        ReservationTime reservationTime = timeRepository.findById(reservation.getTimeId())
                .orElseThrow();

        return Optional.of(new ReservationDetail(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getThemeId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImgUrl(),
                reservation.getTimeId(),
                reservationTime.getStartAt()
        ));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation savedReservation = reservation.withId(idHolder);
        reservations.put(idHolder++, savedReservation);
        return savedReservation;
    }

    @Override
    public Reservation update(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Integer delete(Long id) {
        int beforeSize = reservations.size();
        reservations.remove(id);
        int afterSize = reservations.size();

        if (beforeSize != afterSize) {
            return 1;
        }

        return 0;
    }

    @Override
    public Boolean existsByDateAndThemeAndTime(LocalDate date, Long themeId, Long timeId) {
        return reservations.values().stream()
                .anyMatch(savedReservation -> (savedReservation.getThemeId().equals(themeId) &&
                        savedReservation.getTimeId().equals(timeId) &&
                        savedReservation.getDate().equals(date)));
    }

    @Override
    public Boolean existsByDateAndThemeAndTimeExcludingId(LocalDate date, Long themeId, Long timeId, Long id) {
        return reservations.values().stream()
                .filter(savedReservation -> !savedReservation.getId().equals(id))
                .anyMatch(savedReservation -> savedReservation.getThemeId().equals(themeId)
                        && savedReservation.getTimeId().equals(timeId)
                        && savedReservation.getDate().equals(date));
    }

    public List<Reservation> findAllReservations() {
        return reservations.values().stream().toList();
    }
}
