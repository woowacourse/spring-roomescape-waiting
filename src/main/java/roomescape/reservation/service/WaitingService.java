package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservation.exception.WaitingNotFoundException;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }

    public List<ReservationResponse> findWaitings() {
        return waitingRepository.findAll()
                .stream()
                .map(ReservationResponse::of)
                .toList();
    }

    public List<WaitingWithRank> findMyWaitingsWithRank(UserInfo userInfo) {
        return waitingRepository.findWaitingsWithRankByMemberId(userInfo.id());
    }

    public int findMaxOrderByDateAndTimeAndTheme(final LocalDate date, final Long timeId, final Long themeId) {
        return waitingRepository.findMaxOrderByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public Waiting save(final Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    public Waiting findFirstWaitingOfInfo(ReservationInfo reservationInfo) {
        return waitingRepository.findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(reservationInfo.getDate(),
                        reservationInfo.getTime(), reservationInfo.getTheme())
                .orElseThrow(() -> new WaitingNotFoundException("요청한 id와 일치하는 대기 정보가 없습니다."));
    }

    public boolean isWaitingExists(final ReservationInfo info) {
        return waitingRepository.existsByDateAndTimeIdAndThemeId(info.getDate(), info.getIdOfTime(),
                info.getIdOfTheme());
    }

}
