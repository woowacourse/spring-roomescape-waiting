package roomescape.reservation.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationUpdateService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationUpdateService(ReservationRepository reservationRepository,
                                    MemberRepository memberRepository,
                                    TimeRepository timeRepository,
                                    ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberByMemberId(request.memberId());
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    public ReservationResponse createReservation(ReservationCreateRequest request, Long memberId) {
        Member member = findMemberByMemberId(memberId);
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    private ReservationResponse createReservation(Reservation reservation) {
        validateIsAfterFromNow(reservation);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    private void validateIsAfterFromNow(Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadArgumentRequestException("예약은 현재 시간 이후여야 합니다.");
        }
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 멤버가 존재하지 않습니다."));
    }

    private ReservationTime findTimeByTimeId(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme findThemeByThemeId(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 테마가 존재하지 않습니다."));
    }
}
