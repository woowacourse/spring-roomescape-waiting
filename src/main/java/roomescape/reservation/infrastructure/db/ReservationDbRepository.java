package roomescape.reservation.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.infrastructure.db.dao.ReservationJpaRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;

@Repository
@RequiredArgsConstructor
public class ReservationDbRepository implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public List<Reservation> getAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void remove(Reservation reservation) {
        reservationJpaRepository.delete(reservation);
    }

    @Override
    public boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public boolean existsByThemeId(Long reservationThemeId) {
        return reservationJpaRepository.existsByThemeId(reservationThemeId);
    }

    @Override
    public boolean existsByTimeId(Long reservationTimeId) {
        return reservationJpaRepository.existsByTimeId(reservationTimeId);
    }

    @Override
    public List<Reservation> getSearchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        Specification<Reservation> spec = Specification.where(
            ReservationSpecification.memberIdEquals(memberId))
            .and(ReservationSpecification.themeIdEquals(themeId))
            .and(ReservationSpecification.betweenDate(from, to));

        List<Reservation> reservations = reservationJpaRepository.findAll(spec);
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public Reservation getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 예약이 존재하지 않습니다."));
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationJpaRepository.findAllByMemberId(memberId);
    }
}
