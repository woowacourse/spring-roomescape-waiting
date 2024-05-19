package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.MemberReservationRequest;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              MemberReservationRepository memberReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationRepository.findBy(request.getMemberId(), request.getThemeId(), request.getStartDate(),
                        request.getEndDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findMyReservations(AuthInfo authInfo) {
        Member member = getMember(authInfo.getId());
        return memberReservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse createMemberReservation(AuthInfo authInfo, ReservationRequest reservationRequest) {
        LocalDate date = LocalDate.parse(reservationRequest.date());
        return createMemberReservation(
                authInfo.getId(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                date
        );
    }

    @Transactional
    public ReservationResponse createMemberReservation(MemberReservationRequest memberReservationRequest) {
        LocalDate date = LocalDate.parse(memberReservationRequest.date());
        return createMemberReservation(
                memberReservationRequest.memberId(),
                memberReservationRequest.timeId(),
                memberReservationRequest.themeId(),
                date
        );
    }

    private ReservationResponse createMemberReservation(long memberId, long timeId, long themeId, LocalDate date) {
        ReservationTime reservationTime = getReservationTime(timeId);
        Theme theme = getTheme(themeId);
        Member member = getMember(memberId);
        Reservation reservation = getReservation(date, reservationTime, theme);

        if (reservation.isPast()) {
            throw new BadRequestException("올바르지 않는 데이터 요청입니다.");
        }

        if (memberReservationRepository.existsByReservationAndMember(reservation, member)) {
            throw new ForbiddenException("중복된 예약입니다.");
        }

        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(member, reservation));
        return ReservationResponse.from(memberReservation.getId(), reservation, member);
    }

    public void deleteMemberReservation(AuthInfo authInfo, long memberReservationId) {
        MemberReservation memberReservation = getMemberReservation(memberReservationId);
        Member member = getMember(authInfo.getId());
        if (!member.isAdmin() && !memberReservation.isMember(member)) {
            throw new ForbiddenException("예약자가 아닙니다.");
        }
        memberReservationRepository.deleteById(memberReservationId);
    }

    @Transactional
    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservation_Id(reservationId);
        reservationRepository.deleteById(reservationId);
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BadRequestException("해당 ID에 대응되는 예약 시간이 없습니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BadRequestException("해당 ID에 대응되는 테마가 없습니다."));
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("해당 유저를 찾을 수 없습니다."));
    }

    private Reservation getReservation(LocalDate date, ReservationTime time, Theme theme) {
        return reservationRepository.findByDateAndTimeAndTheme(date, time, theme)
                .orElse(reservationRepository.save(new Reservation(date, time, theme)));
    }

    private MemberReservation getMemberReservation(long memberReservationId) {
        return memberReservationRepository.findById(memberReservationId)
                .orElseThrow(() -> new BadRequestException("해당 ID에 대응되는 사용자 예약이 없습니다."));
    }
}
