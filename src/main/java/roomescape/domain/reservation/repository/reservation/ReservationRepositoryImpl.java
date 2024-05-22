package roomescape.domain.reservation.repository.reservation;

import static roomescape.domain.reservation.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationWithOrderDto;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryImpl(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return jpaReservationRepository.findByThemeIdAndMemberIdAndDateValueBetween(themeId, memberId, dateFrom,
                dateTo);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return jpaReservationRepository.existsByDateValueAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationRepository.findByDateValueAndThemeId(date, themeId);
    }

    @Override
    public List<ReservationWithOrderDto> findByMemberId(Long memberId) {
        return jpaReservationRepository.findByMemberId(memberId);
    }

    @Override
    public boolean existByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId) {
        return jpaReservationRepository.existsByMemberIdAndDateValueAndTimeIdAndThemeId(memberId, date,
                timeId, themeId);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return jpaReservationRepository.findByStatus(status);
    }

    @Override
    public Optional<Reservation> findTopWaitingReservationBy(LocalDate date, Long timeId, Long themeId) {
        return jpaReservationRepository.findTop1ByDateValueAndThemeIdAndTimeIdAndStatus(date, timeId, themeId, WAITING);
    }
}
