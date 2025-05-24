package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations;

    private AtomicLong index = new AtomicLong(0);

    public FakeReservationRepository(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                             LocalDate dateTo) {
        return reservations.stream()
                .filter(reservation -> matchMemberId(reservation, memberId))
                .filter(reservation -> matchThemeId(reservation, themeId))
                .filter(reservation -> matchDateRange(reservation, dateFrom, dateTo))
                .toList();
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.timeId().equals(timeId));
    }

    @Override
    public boolean hasReservedReservation(Reservation reservation) {
        return reservations.stream()
                .anyMatch(nextReservation ->
                        nextReservation.timeId().equals(reservation.timeId())
                                && nextReservation.getDate().equals(reservation.getDate())
                                && nextReservation.themeId().equals(reservation.themeId())
                                && nextReservation.getStatus().equals(ReservationStatus.RESERVED));
    }

    @Override
    public boolean hasSameReservation(Reservation reservation) {
        return reservations.stream()
                .anyMatch(nextReservation ->
                        nextReservation.timeId().equals(reservation.timeId())
                                && nextReservation.getDate().equals(reservation.getDate())
                                && nextReservation.themeId().equals(reservation.themeId())
                                && nextReservation.timeId().equals(reservation.timeId())
                                && nextReservation.getStatus().equals(reservation.getStatus()));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.themeId().equals(themeId));
    }

    @Override
    public List<ReservationWithRank> findReservationWithRankByMemberId(Long memberId) {
        List<Reservation> memberReservations = reservations.stream()
                .filter(reservation -> reservation.memberId().equals(memberId))
                .toList();

        return memberReservations.stream()
                .map(reservation -> new ReservationWithRank(reservation, findRank(reservation)))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findAny();
    }

    @Override
    public void changeReservationStatus(Long id, ReservationStatus status) {
        Optional<Reservation> findReservation = reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findAny();
        if (findReservation.isEmpty()) {
            return;
        }
        Reservation beforeReservation = findReservation.get();
        Reservation reservation = Reservation.createWithId(beforeReservation.getId(), beforeReservation.getMember(),
                beforeReservation.getDate(), beforeReservation.getTime(), beforeReservation.getTheme(),
                status, beforeReservation.getCreatedAt());
        reservations.remove(findReservation.get());
        reservations.add(reservation);
    }

    private Long findRank(Reservation memberReservation) {
        return reservations.stream()
                .filter(reservation -> reservation.timeId().equals(memberReservation.timeId()))
                .filter(reservation -> reservation.themeId().equals(memberReservation.themeId()))
                .filter(reservation -> reservation.getDate().equals(memberReservation.getDate()))
                .filter(reservation -> reservation.getCreatedAt().isBefore(memberReservation.getCreatedAt()))
                .count();
    }


    @Override
    public Reservation save(Reservation reservation) {
        long currentIndex = index.incrementAndGet();

        reservations.add(reservation.assignId(currentIndex));
        return reservation.assignId(currentIndex);
    }

    @Override
    public void deleteById(Long id) {
        Optional<Reservation> findReservation = reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findAny();

        if (findReservation.isEmpty()) {
            return;
        }

        Reservation reservation = findReservation.get();
        reservations.remove(reservation);

    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.themeId().equals(themeId))
                .toList();
    }

    @Override
    public List<Reservation> findAllWaitingReservations(LocalDateTime now) {
        return reservations.stream()
                .filter(Reservation::isWaitingStatus)
                .filter(reservation -> checkFutureDateTime(now, reservation))
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    private boolean matchMemberId(Reservation reservation, Long memberId) {
        return memberId == null || reservation.memberId().equals(memberId);
    }

    private boolean matchThemeId(Reservation reservation, Long themeId) {
        return themeId == null || reservation.themeId().equals(themeId);
    }

    private boolean matchDateRange(Reservation reservation, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo == null) {
            return true;
        }
        LocalDate reservationDate = reservation.getDate();
        if (dateFrom != null && reservationDate.isBefore(dateFrom)) {
            return false;
        }
        if (dateTo != null && reservationDate.isAfter(dateTo)) {
            return false;
        }
        return true;
    }

    private boolean checkFutureDateTime(LocalDateTime now, Reservation reservation) {
        LocalDateTime dateTime = LocalDateTime.of(reservation.getDate(), reservation.reservationTime());
        return dateTime.isAfter(now);
    }
}
