package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberQueryService;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.exception.AlreadyWaitingException;
import roomescape.reservation.repository.ReservationWaitingRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeQueryService;
import roomescape.time.service.ReservationTimeQueryService;

@Service
public class ReservationWaitingService {

    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;
    private final ReservationTimeQueryService timeQueryService;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ThemeQueryService themeQueryService, MemberQueryService memberQueryService,
                                     ReservationTimeQueryService timeQueryService,
                                     ReservationWaitingRepository reservationWaitingRepository) {
        this.themeQueryService = themeQueryService;
        this.memberQueryService = memberQueryService;
        this.timeQueryService = timeQueryService;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

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

    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return reservationWaitingRepository.findWithRankByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
