package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingRankReservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<WaitingRankReservation> findAllWaitingRankByMember(final Member member) {
        return reservationJpaRepository.findAllByMember(member);
    }

    @Override
    public List<Reservation> findAllByThemeAndDate(final Theme theme, final ReservationDate date) {
        return reservationJpaRepository.findAllByThemeAndDate(theme, date);
    }

    @Override
    public List<Reservation> findAllByMemberAndThemeAndDateBetween(final Member member, final Theme theme,
                                                                   final ReservationDate from,
                                                                   final ReservationDate to) {
        return reservationJpaRepository.findAllByMemberAndThemeAndDateBetween(member, theme, from, to);
    }

    @Override
    public List<Reservation> findAllByReservationStatus(final ReservationStatus reservationStatus) {
        return reservationJpaRepository.findAllByReservationStatus(reservationStatus);
    }

    @Override
    public List<Reservation> findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByAsc(final ReservationDate date,
                                                                                         final ReservationTime reservationTime,
                                                                                         final Theme theme,
                                                                                         final ReservationStatus reservationStatus) {
        return reservationJpaRepository.findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByIdAsc(date, reservationTime,
                theme,
                reservationStatus);
    }

    @Override
    public void deleteById(final Long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByReservationTime(final ReservationTime reservationTime) {
        return reservationJpaRepository.existsByReservationTime(reservationTime);
    }

    @Override
    public boolean existsByDuplicateMember(final ReservationDate date,
                                           final ReservationTime reservationTime,
                                           final Theme theme, final Member member) {
        return reservationJpaRepository.existsByDateAndReservationTimeAndThemeAndMember(date, reservationTime, theme,
                member);
    }

    @Override
    public boolean existsDuplicateStatus(
            final ReservationTime reservationTime,
            final ReservationDate date,
            final Theme theme,
            final ReservationStatus reservationStatus
    ) {
        return reservationJpaRepository.existsByReservationTimeAndDateAndThemeAndReservationStatus(reservationTime, date,
                theme, reservationStatus);
    }
}
