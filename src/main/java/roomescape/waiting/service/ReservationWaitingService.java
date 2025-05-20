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
import roomescape.reservation.exception.AlreadyWaitingException;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.repository.ReservationWaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationWaitingService implements ReservationWaitingQueryService {

    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;
    private final ReservationTimeQueryService timeQueryService;
    private final ReservationWaitingRepository reservationWaitingRepository;

    @Transactional
    public ReservationResponse waiting(ReserveCommand reserveCommand) {
        ReservationWaiting reservationWaiting = reservationWaitingFrom(reserveCommand);

        ReservationWaiting saved = reservationWaitingRepository.save(reservationWaiting);

        return ReservationResponse.from(saved);
    }

    private ReservationWaiting reservationWaitingFrom(ReserveCommand reserveCommand) {
        Long memberId = reserveCommand.memberId();
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();
        if (reservationWaitingRepository.existsByMemberIdAndDateAndTimeId(memberId, date, memberId)) {
            throw new AlreadyWaitingException("이미 예약된 시간입니다.");
        }

        ReservationDateTime reservationDateTime = ReservationDateTime.create(new ReservationDate(date),
                timeQueryService.getReservationTime(timeId));
        Theme theme = themeQueryService.getTheme(reserveCommand.themeId());
        Member reserver = memberQueryService.getMember(memberId);

        return ReservationWaiting.builder()
                .reservationDatetime(reservationDateTime)
                .reserver(reserver)
                .theme(theme)
                .build();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return reservationWaitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByUser(Long id, Long memberId) {
        if (!reservationWaitingRepository.existsByIdAndReserverId(id, memberId)) {
            throw new NotAuthorizationException("해당 예약자가 아닙니다.");
        }

        delete(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!reservationWaitingRepository.existsById(id)) {
            throw new NotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }

        reservationWaitingRepository.deleteById(id);
    }
}
