package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;
    private final ReservationTimeJpaRepository timeJpaRepository;

    public ReservationRepositoryAdapter(ReservationJpaRepository jpaRepository,
                                        ReservationTimeJpaRepository timeJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.timeJpaRepository = timeJpaRepository;
    }

    @Override
    public List<Reservation> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        // delete-before-insert 보장: JPA 쓰기지연은 flush 시 INSERT→UPDATE→DELETE로 재정렬한다.
        // 승급 흐름(취소→같은 슬롯 대기 승격)에서 IDENTITY save가 즉시 INSERT되므로, 옇 예약 DELETE를
        // 먼저 flush하지 않으면 UNIQUE(date,time_id,theme_id) 충돌. JDBC의 즉시 DELETE 의미를 복원.
        jpaRepository.deleteById(id);
        jpaRepository.flush();
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return jpaRepository.existsByDateAndTime_IdAndTheme_Id(date, timeId, themeId);
    }

    @Override
    public boolean existsBySlotAndName(LocalDate date, Long timeId, Long themeId, String name) {
        return jpaRepository.existsByDateAndTime_IdAndTheme_IdAndName(date, timeId, themeId, name);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return jpaRepository.existsByTime_Id(timeId);
    }

    @Override
    public List<Reservation> findByNameOrderByDateAscTimeAsc(String name) {
        return jpaRepository.findByNameOrderByDateAscTime_StartAtAsc(name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByDateAndTimeAndThemeExcludingId(LocalDate date, Long timeId, Long themeId, Long excludeId) {
        return jpaRepository.existsByDateAndTime_IdAndTheme_IdAndIdNot(date, timeId, themeId, excludeId);
    }

    @Override
    public void updateDateAndTime(Long id, LocalDate date, Long timeId) {
        // 과도기 의미 보존: bulk update. time 은 식별자 프록시로 참조.
        jpaRepository.updateDateAndTime(id, date, timeJpaRepository.getReferenceById(timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return jpaRepository.existsByTheme_Id(themeId);
    }
}
