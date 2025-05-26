package roomescape.reservation.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.global.exception.custom.NotFoundException;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationWithMemberRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationCommandService(final ReservationRepository reservationRepository,
                                     final ReservationTimeRepository reservationTimeRepository,
                                     final ThemeRepository themeRepository,
                                     final MemberRepository memberRepository,
                                     final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse createMyReservation(final CreateReservationRequest request,
                                                   final LoginMember loginMember) {
        final Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new UnauthorizedException("예약자를 찾을 수 없습니다."));
        final Reservation reservation = convertToReservation(
                request.themeId(),
                request.timeId(),
                request.date(),
                member
        );
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public ReservationResponse createReservationByAdmin(final CreateReservationWithMemberRequest request) {
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BadRequestException("예약자를 찾을 수 없습니다."));
        final Reservation reservation = convertToReservation(
                request.themeId(),
                request.timeId(),
                request.date(),
                member
        );
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void cancelReservationById(final long id) {
        waitingRepository.findFirstByReservationIdOrderByCreatedAtAsc(id)
                .ifPresentOrElse(
                        (waiting) -> processWaitingToReservation(id, waiting),
                        () -> reservationRepository.deleteById(id)
                );
    }

    private void processWaitingToReservation(final long id, final Waiting waiting) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
        reservation.updateMember(waiting.getMember());
        waitingRepository.delete(waiting);
    }

    private Reservation convertToReservation(
            final long themeId,
            final long timeId,
            final LocalDate date,
            final Member member) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new BadRequestException("테마가 존재하지 않습니다."));
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BadRequestException("예약 시간이 존재하지 않습니다."));
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(),
                theme.getId())) {
            throw new BadRequestException("해당 시간에 이미 예약이 존재합니다.");
        }
        return Reservation.register(member, date, time, theme);
    }
}
