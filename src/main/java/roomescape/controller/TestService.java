package roomescape.controller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;

import java.time.LocalTime;
import java.util.List;


/***
 * 양방향 매핑 테스트 서비스
 *
 * 1단계 이후 삭제할 예정
 */

@Service
@Transactional(readOnly = true)
public class TestService {

    private final SlotRepository repository;

    public TestService(SlotRepository repository) {
        this.repository = repository;
    }

    public Slot test1() {
        return repository.getById(1L);
    }

    public LocalTime test2() {
        return repository.getById(1L).getTime().getStartAt();
    }
}
