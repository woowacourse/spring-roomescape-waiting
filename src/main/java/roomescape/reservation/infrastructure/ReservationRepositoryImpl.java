package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

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
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationRepository.findByDateAndTheme_Id(date, themeId);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findByMemberId(Long id) {
        return jpaReservationRepository.findByMember_Id(id);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        return jpaReservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, dateFrom, dateTo);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return jpaReservationRepository.existsByTime_Id(timeId);
    }

    @Override
    public boolean existsByDateAndTimeStartAtAndThemeId(LocalDate date, LocalTime time, Long themeId) {
        return jpaReservationRepository.existsByDateAndTime_StartAtAndTheme_Id(date, time, themeId);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return jpaReservationRepository.existsByTheme_Id(themeId);
    }
}
