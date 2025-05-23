package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservationTime.entity.ReservationTime;
import roomescape.reservationTime.repository.JpaReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public ReservationService(
            final JpaReservationRepository reservationRepository,
            final JpaReservationTimeRepository reservationTimeRepository,
            final JpaThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByMemberId(final Member member) {
        return reservationRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new NotFoundException("reservations"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByFilters(
            final long themeId,
            final long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        return reservationRepository.findReservationsByFilters(themeId, memberId, dateFrom, dateTo)
                .orElseThrow(() -> new NotFoundException("reservation"));
    }

    @Transactional
    public Reservation addReservation(
            final Member member,
            final LocalDate date,
            final long themeId,
            final long timeId
    ) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("theme"));

        validateDuplicateReservation(date,timeId,themeId);
        validateDateTimeAfterNow(date, time);

        return reservationRepository.save(new Reservation(member, date, time, theme));
    }


    private void validateDateTimeAfterNow(final LocalDate date, final ReservationTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (date.isBefore(now.toLocalDate()) ||
                (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    @Transactional(readOnly = true)
    private void validateDuplicateReservation(
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new DuplicatedException("reservation");
        }
    }

    public Reservation removeReservation(final long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reservation"));
        reservationRepository.deleteById(id);

        return reservation;
    }
}
