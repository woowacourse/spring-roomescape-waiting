package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.repository.jpa.ReservationJpaRepository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(final ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<Reservation> findAllReservationsV2() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Reservation saveWithMember(final Reservation reservation) {
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
