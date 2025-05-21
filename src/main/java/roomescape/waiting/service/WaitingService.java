package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.global.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.service.ReservationQueryManager;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService implements WaitingQueryService {

    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;
    private final ReservationTimeQueryService timeQueryService;
    private final WaitingRepository waitingRepository;
    private final ReservationQueryManager reservationQueryManager;

    @Transactional
    public ReservationResponse waiting(ReserveCommand reserveCommand) {
        Waiting waiting = reservationWaitingFrom(reserveCommand);

        Waiting saved = waitingRepository.save(waiting);

        return ReservationResponse.from(saved);
    }

    private Waiting reservationWaitingFrom(ReserveCommand reserveCommand) {
        Long memberId = reserveCommand.memberId();
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();
        validateWaiting(memberId, date, timeId);

        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(date),
                timeQueryService.getReservationTime(timeId));
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member reserver = memberQueryService.getMember(memberId);

        return Waiting.builder()
                .reservationDatetime(reservationDateTime)
                .reserver(reserver)
                .theme(theme)
                .build();
    }

    private void validateWaiting(Long memberId, LocalDate date, Long timeId) {
        if (waitingRepository.existsByMemberIdAndDateAndTimeId(memberId, date, memberId)) {
            throw new InAlreadyWaitingException("이미 예약된 시간입니다.");
        }

        if (reservationQueryManager.existReservation(memberId, date, timeId)) {
            throw new InAlreadyReservationException("해당 유저는 이미 예약했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return waitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByUser(Long id, Long memberId) {
        if (!waitingRepository.existsByIdAndReserverId(id, memberId)) {
            throw new NotAuthorizationException("해당 예약자가 아닙니다.");
        }

        delete(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!waitingRepository.existsById(id)) {
            throw new NotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }

        waitingRepository.deleteById(id);
    }
}
