package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.repository.jpa.ReservationJpaRepository;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void deleteById(final long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<Reservation> findFirstByReservationItemAndReservationStatusOrderByIdAsc(ReservationItem reservationItem,
                                                                                     ReservationStatus reservationStatus) {
        return reservationJpaRepository.findFirstByReservationItemAndReservationStatusOrderByIdAsc(reservationItem, reservationStatus);
    }

    @Override
    public long countByReservationItemIdAndIdLessThan(Long reservationItemId, Long currentReservationId) {
        return reservationJpaRepository.countByReservationItemIdAndIdLessThan(reservationItemId, currentReservationId);
    }

    @Override
    public boolean existsByMemberAndReservationItem(Member member, ReservationItem reservationItem) {
        return reservationJpaRepository.existsByMemberAndReservationItem(member, reservationItem);
    }

    @Override
    public List<Reservation> findByReservationStatusOrderByIdDesc(ReservationStatus reservationStatus) {
        return reservationJpaRepository.findByReservationStatusOrderByIdDesc(reservationStatus);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return reservationJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final Long memberId,
                                                                          final Long themeId,
                                                                          final LocalDate dateFrom,
                                                                          final LocalDate dateTo) {
        return reservationJpaRepository.findByMemberIdAndThemeIdAndDateFromAndDateTo(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
    }
}
