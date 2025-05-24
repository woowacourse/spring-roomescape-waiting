package roomescape.reservation.infrastructure.db;

import static roomescape.reservation.model.entity.vo.ReservationStatus.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.infrastructure.db.dao.ReservationJpaRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.vo.ReservationStatus;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.vo.Schedule;

@Repository
@RequiredArgsConstructor
public class ReservationDbRepository implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public List<Reservation> getAllByStatuses(List<ReservationStatus> reservationStatuses) {
        return reservationJpaRepository.findAllByStatusIn(reservationStatuses);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return reservationJpaRepository.findById(reservationId);
    }

    @Override
    public Reservation getById(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 예약이 존재하지 않습니다."));
    }

    @Override
    public boolean existDuplicatedSchedule(Schedule schedule) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeIdAndStatus(schedule.date(), schedule.timeId(), schedule.themeId(), CONFIRMED);
    }

    @Override
    public boolean existsActiveByThemeId(Long reservationThemeId) {
        return reservationJpaRepository.existsByThemeIdAndDateGreaterThanEqual(reservationThemeId, LocalDate.now());
    }

    @Override
    public boolean existsActiveByTimeId(Long reservationTimeId) {
        return reservationJpaRepository.existsByTimeIdAndDateGreaterThanEqual(reservationTimeId, LocalDate.now());
    }

    @Override
    public List<Reservation> getSearchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        Specification<Reservation> spec = Specification.where(
                        ReservationSpecification.memberIdEquals(memberId))
                .and(ReservationSpecification.themeIdEquals(themeId))
                .and(ReservationSpecification.betweenDate(from, to));

        return reservationJpaRepository.findAll(spec);
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationJpaRepository.findAllByMemberId(memberId);
    }
}
