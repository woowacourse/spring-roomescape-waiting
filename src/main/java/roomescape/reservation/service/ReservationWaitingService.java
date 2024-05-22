package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.member.model.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.ReservationWaitingDto;
import roomescape.reservation.dto.ReservationWaitingWithOrderDto;
import roomescape.reservation.dto.SaveReservationWaitingRequest;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationWaiting;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationWaitingService(
            final MemberService memberService,
            final ReservationWaitingRepository reservationWaitingRepository,
            final ReservationRepository reservationRepository,
            final ThemeService themeService,
            final ReservationTimeService reservationTimeService
    ) {
        this.memberService = memberService;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    public List<ReservationWaitingDto> getAllReservationWaiting() {
        return reservationWaitingRepository.findAll()
                .stream()
                .map(ReservationWaitingDto::from)
                .toList();
    }

    public List<ReservationWaitingWithOrderDto> getMyReservationWaiting(final Long memberId) {
        final List<ReservationWaiting> reservationWaiting = reservationWaitingRepository.findAllByMemberId(memberId);
        return reservationWaiting.stream()
                .map(waiting -> ReservationWaitingWithOrderDto.from(waiting, parseReservationWaitingOrder(waiting)))
                .toList();
    }

    private int parseReservationWaitingOrder(final ReservationWaiting reservationWaiting) {
        final int beforeWaitingCount = reservationWaitingRepository.countAllByThemeAndDateAndTimeAndCreatedAtBefore(
                reservationWaiting.getTheme(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime(),
                reservationWaiting.getCreatedAt()
        );
        return beforeWaitingCount + 1;
    }

    public Long saveReservationWaiting(final SaveReservationWaitingRequest request) {
        final ReservationTime reservationTime = reservationTimeService.getReservationTime(request.time());
        final Theme theme = themeService.getTheme(request.theme());
        final Member member = memberService.getMember(request.memberId());
        final ReservationDate reservationDate = new ReservationDate(request.date());

        checkReservationExist(theme, reservationDate, reservationTime);
        checkReservationWaitingAlreadyExist(member, reservationDate, reservationTime, theme);

        final ReservationWaiting reservationWaiting = new ReservationWaiting(
                reservationTime,
                theme,
                member,
                reservationDate,
                LocalDateTime.now()
        );
        return reservationWaitingRepository.save(reservationWaiting).getId();
    }

    private void checkReservationExist(
            final Theme theme,
            final ReservationDate date,
            final ReservationTime reservationTime
    ) {
        if (!reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, reservationTime.getId(), theme.getId())) {
            throw new IllegalStateException("존재하지 않는 예약에 대한 대기 신청을 할 수 없습니다.");
        }
    }

    private void checkReservationWaitingAlreadyExist(
            final Member member,
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        if (reservationWaitingRepository.existsByMemberAndDateAndTimeAndTheme(member, reservationDate, reservationTime, theme)) {
            throw new IllegalStateException("이미 해당 예약 대기가 존재합니다.");
        }
    }

    public void deleteReservationWaiting(final Long reservationWaitingId) {
        reservationWaitingRepository.deleteById(reservationWaitingId);
    }
}
