package roomescape.reservation.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.exception.WaitingNotFoundException;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class WaitingModuleService {

    private final WaitingReservationRepository waitingReservationRepository;

    public WaitingModuleService(final WaitingReservationRepository waitingReservationRepository) {
        this.waitingReservationRepository = waitingReservationRepository;
    }

    public void delete(Long id) {
        waitingReservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findWaitings() {
        return waitingReservationRepository.findAll()
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getInfo().getTime();
                    Theme theme = reservation.getInfo().getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    public List<WaitingWithRank> findMyWaitingsWithRank(UserInfo userInfo) {
        return waitingReservationRepository.findWaitingsWithRankByMemberId(userInfo.id());
    }

    public int findMaxOrderByDateAndTimeAndTheme(final LocalDate date, final Long timeId, final Long themeId) {
        return waitingReservationRepository.findMaxOrderByDateAndTimeAndTheme(date, timeId, themeId);
    }

    public Waiting save(final Waiting waiting) {
        return waitingReservationRepository.save(waiting);
    }

    public Waiting findFirstWaitingOfInfo(ReservationInfo reservationInfo) {
        return waitingReservationRepository.findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(reservationInfo.getDate(),
                        reservationInfo.getTime(), reservationInfo.getTheme())
                .orElseThrow(() -> new WaitingNotFoundException("요청한 id와 일치하는 대기 정보가 없습니다."));
    }

    public boolean isWaitingExists(final ReservationInfo info) {
        if (waitingReservationRepository.existsByDateAndTimeIdAndThemeId(info.getDate(),info.getTime().getId(),info.getTheme().getId())) {
            return true;
        }
        return false;
    }

}
