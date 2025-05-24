package roomescape.reservation.application.waiting.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.waiting.dto.ReservationWaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(final ReservationWaitingRepository reservationWaitingRepository, final ReservationRepository reservationRepository,
                                     final ReservationTimeRepository reservationTimeRepository, final ThemeRepository themeRepository,
                                     final MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationWaitingInfo createReservationWaiting(final ReservationWaitingCreateCommand command) {
        final ReservationWaiting reservationWaiting = makeReservationWaiting(command);
        final ReservationWaiting savedReservationWaiting = reservationWaitingRepository.save(reservationWaiting);
        return new ReservationWaitingInfo(savedReservationWaiting);
    }

    private ReservationWaiting makeReservationWaiting(final ReservationWaitingCreateCommand command) {
        final ReservationTime reservationTime = findReservationTime(command.timeId());
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        validateDuplicateReservationWaiting(command.date(), command.timeId(), command.themeId(), member.id());
        return command.convertToEntity(command.date(), reservationTime, theme, member);
    }

    private void validateDuplicateReservationWaiting(final LocalDate date, final long timeId, final long themeId, final long memberId) {
        if (reservationWaitingRepository.existsByReservationAndMemberId(date, timeId, themeId, memberId)) {
            throw new IllegalArgumentException("해당 예약 대기에 이미 대기가 존재합니다.");
        }
    }

    public void cancelReservationWaitingById(final long id) {
        reservationWaitingRepository.deleteById(id);
    }

    public List<ReservationWaitingInfo> findAll() {
        return reservationWaitingRepository.findAll()
                .stream()
                .map(ReservationWaitingInfo::new)
                .toList();
    }

    private ReservationTime findReservationTime(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }
}
