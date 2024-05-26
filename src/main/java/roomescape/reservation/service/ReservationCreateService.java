package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class ReservationCreateService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationCreateService(ReservationRepository reservationRepository,
                                    MemberRepository memberRepository,
                                    TimeRepository timeRepository,
                                    ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ReservationResponse execute(ReservationCreateRequest request) {
        Reservation reservation = makeReservation(
                request.memberId(), request.date(), request.timeId(), request.themeId());
        return saveReservation(reservation);
    }

    @Transactional
    public ReservationResponse execute(ReservationCreateRequest request, Long memberId) {
        Reservation reservation = makeReservation(
                memberId, request.date(), request.timeId(), request.themeId());
        return saveReservation(reservation);
    }

    private Reservation makeReservation(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Member member = findMemberByMemberId(memberId);
        ReservationTime time = findTimeByTimeId(timeId);
        Theme theme = findThemeByThemeId(themeId);
        return new Reservation(member, date, time, theme);
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

    private ReservationResponse saveReservation(Reservation reservation) {
        validateIsAfterFromNow(reservation);

        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    private void validateIsAfterFromNow(Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadArgumentRequestException("예약은 현재 시간 이후여야 합니다.");
        }
    }
}
