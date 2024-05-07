package roomescape.service.fakedao;

import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dto.ReservationRowDto;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakeReservationDao implements ReservationDao {

    private final AtomicLong index = new AtomicLong(1);
    private final List<ReservationRowDto> reservations = new ArrayList<>();

    public FakeReservationDao(List<ReservationRowDto> reservations) {
        reservations.forEach(this::save);
    }

    @Override
    public List<ReservationRowDto> findAll() {
        return Collections.unmodifiableList(reservations);
    }

    @Override
    public long save(ReservationRowDto rawDto) {
        long key = index.getAndIncrement();
        ReservationRowDto reservationRowDto = new ReservationRowDto(
                key, rawDto.getDate(),
                rawDto.getTimeId(), rawDto.getThemeId(), rawDto.getMemberId());
        reservations.add(reservationRowDto);
        return key;
    }

    @Override
    public Optional<ReservationRowDto> findById(long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst();
    }

    @Override
    public List<ReservationRowDto> findByDateAndThemeId(LocalDate date, long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getThemeId() == themeId)
                .toList();
    }

    @Override
    public List<Long> findThemeIdByDateAndOrderByThemeIdCountAndLimit(LocalDate startDate, LocalDate endDate, int limit) {
        List<ReservationRowDto> filteredReservations = findBetweenDates(startDate, endDate);
        Map<Long, Long> countOfThemeIds = countByThemeId(filteredReservations);
        return sortByCountAndLimit(countOfThemeIds, limit);
    }

    private List<ReservationRowDto> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        return reservations.stream()
                .filter(reservation -> isBetweenDate(reservation.getDate(), startDate, endDate))
                .toList();
    }

    private boolean isBetweenDate(LocalDate target, LocalDate startDate, LocalDate endDate) {
        return target.isAfter(startDate) || target.isEqual(startDate)
                && target.isBefore(endDate) || target.isEqual(endDate);
    }

    private Map<Long, Long> countByThemeId(List<ReservationRowDto> filteredReservations) {
        return filteredReservations.stream()
                .collect(Collectors.groupingBy(ReservationRowDto::getThemeId, Collectors.counting()));
    }

    private List<Long> sortByCountAndLimit(Map<Long, Long> countOfThemeIds, int limit) {
        return countOfThemeIds.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public void deleteById(long id) {
        ReservationRowDto foundReservation = reservations.stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
        reservations.remove(foundReservation);
    }

    @Override
    public Boolean isExistById(long id) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getId() == id);
    }

    @Override
    public Boolean isExistByTimeId(long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTimeId() == timeId);
    }

    @Override
    public Boolean isExistByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().isEqual(date)
                        && reservation.getTimeId() == timeId
                        && reservation.getThemeId() == themeId);
    }

    @Override
    public List<ReservationRowDto> findByMemberIdAndThemeIdAndDate(long memberId, long themeId, LocalDate from, LocalDate to) {
        return reservations.stream()
                .filter(reservation -> reservation.getMemberId() == memberId
                        && reservation.getThemeId() == themeId
                        && isBetweenDate(reservation.getDate(), from, to))
                .toList();
    }
}