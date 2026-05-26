package roomescape.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ReservationWaitingWithTurn;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationWaitingWithTurn create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = reservationTimeRepository.findBy(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
        Theme theme = themeRepository.findBy(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
        ReservationWaiting waiting = new ReservationWaiting(null, name, date, time, theme);
        // todo: 본인이 예약한 슬롯에 대기 신청 불가 검증
        // todo: 예약이 가능한 슬롯에 대기 신청 불가 검증
        // todo: 지난 날짜/시간에 대기 신청 불가 검증
        // todo: 중복 대기 신청 불가 검증
        Long id = reservationWaitingRepository.insert(waiting);
        ReservationWaiting saved = reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("생성된 예약 대기를 찾을 수 없습니다."));
        Long turn = reservationWaitingRepository.countEarlierWaitings(saved) + 1;
        return new ReservationWaitingWithTurn(
                saved.getId(),
                saved.getName(),
                saved.getDate(),
                saved.getTime(),
                saved.getTheme(),
                turn);
    }

    public List<ReservationWaitingWithTurn> findByName(String name) {
        return reservationWaitingRepository.findByName(name).stream()
                .map(waiting -> {
                    Long turn = reservationWaitingRepository.countEarlierWaitings(waiting) + 1;
                    return new ReservationWaitingWithTurn(
                            waiting.getId(),
                            waiting.getName(),
                            waiting.getDate(),
                            waiting.getTime(),
                            waiting.getTheme(),
                            turn);
                }).toList();
    }

    public void delete(Long id, String name) {
        ReservationWaiting waiting = reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));
        // todo: 본인의 예약 대기인지 검증 로직 추가
        reservationWaitingRepository.delete(id);
    }

}
