package roomescape.time.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeConflictException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.repository.TimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService {

    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final EntityManager em;


    @Override
    @Transactional
    public ReservationTime create(LocalDateTime startAt, LocalDateTime endAt) {
        ReservationTime reservationTime = new ReservationTime(startAt, endAt);
        return timeRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return timeRepository.findAll();
    }

    @Override
    public List<ReservationTime> findByDate(LocalDate date) {
        return timeRepository.findReservationTimeByStartAt(date.atStartOfDay());
    }

    @Override
    public ReservationTime findById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new TimeNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (reservationRepository.existsById(id)) {
            throw new ReservationTimeConflictException(id);
        }
        timeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void writeBehindInsert() {
        log.info("========save 호출 전========");
        ReservationTime time = new ReservationTime(LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        timeRepository.save(time);
        log.info("========save 호출 후========");
    }

    @Override
    @Transactional
    public void writeBehindUpdate() {
        ReservationTime t = timeRepository.findById(1L).get(); // SELECT
        log.info(">>> 필드 수정 전");
        t.updateStart(LocalDateTime.now());
        log.info(">>> 필드 수정 후, commit 전");
    }

    @Transactional
    public void explicitFlush() {
        ReservationTime t = timeRepository.findById(1L).get(); // SELECT
        log.info(">>> 필드 수정 전");
        t.updateStart(LocalDateTime.now());
        em.flush();
        log.info(">>> 필드 수정 후, commit 전");
    }
}
