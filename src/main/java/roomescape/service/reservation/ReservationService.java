package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.*;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidMemberException;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository, ReservationDetailRepository reservationDetailRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationRequest adminReservationRequest) {
        return createReservation(adminReservationRequest.timeId(), adminReservationRequest.themeId(),
                adminReservationRequest.memberId(), adminReservationRequest.date());
    }

    @Transactional
    public ReservationResponse createMemberReservation(ReservationRequest reservationRequest, long memberId) {
        return createReservation(reservationRequest.timeId(), reservationRequest.themeId(), memberId,
                reservationRequest.date());
    }

    private ReservationResponse createReservation(long timeId, long themeId, long memberId, LocalDate date) {
        ReservationDate reservationDate = ReservationDate.of(date);
        ReservationTime reservationTime = findTimeById(timeId);
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        ReservationDetail reservationDetail = getReservationDetail(reservationDate, reservationTime, theme);
        ReservationStatus reservationStatus = determineStatus(reservationDetail, member);
        Reservation reservation = reservationRepository.save(new Reservation(member, reservationDetail, reservationStatus));

        return new ReservationResponse(reservation);
    }

    private ReservationTime findTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 시간입니다."));
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 테마입니다."));
    }

    private Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }

    private ReservationDetail getReservationDetail(ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        Schedule schedule = new Schedule(reservationDate, reservationTime);
        return reservationDetailRepository.findByScheduleAndTheme(schedule, theme)
                .orElseGet(() -> reservationDetailRepository.save(new ReservationDetail(schedule, theme)));
    }

    private ReservationStatus determineStatus(ReservationDetail reservationDetail, Member member) {
        if (reservationRepository.existsByDetailIdAndMemberId(reservationDetail.getId(), member.getId())) {
            throw new InvalidReservationException("이미 예약(대기) 상태입니다.");
        }
        if (reservationRepository.existsByDetailIdAndStatus(reservationDetail.getId(), ReservationStatus.RESERVED)) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.RESERVED;
    }

    @Transactional
    public void deleteById(long id) {
        reservationRepository.findById(id)
                .ifPresent(reservation -> {
                    validateScheduleIfReserved(reservation);
                    reservationRepository.deleteById(id);
                    updateReservation(reservation.getDetail().getId());
                });
    }

    private void validateScheduleIfReserved(Reservation reservation) {
        if (reservation.isReserved() && reservation.isPast()) {
            throw new InvalidReservationException("이미 지난 예약은 삭제할 수 없습니다.");
        }
    }

    private void updateReservation(long detailId) {
        reservationRepository.findFirstByDetailIdOrderByCreatedAt(detailId)
                .ifPresent(Reservation::reserved);
    }

    @Transactional
    public void deleteWaitingById(long reservationId, long memberId) {
        reservationRepository.findById(reservationId)
                .ifPresent(reservation -> {
                    validateStatus(reservation);
                    validateAuthority(reservation, memberId);
                });
        reservationRepository.deleteById(reservationId);
    }

    private void validateStatus(Reservation reservation) {
        if (reservation.getMember().isGuest() && reservation.isReserved()) {
            throw new InvalidReservationException("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요.");
        }
    }

    private void validateAuthority(Reservation reservation, long memberId) {
        if (reservation.getMember().isGuest() && !reservation.isReservationOf(memberId)) {
            throw new ForbiddenException("예약 대기를 삭제할 권한이 없습니다.");
        }
    }

    public List<ReservationResponse> findByCondition(ReservationFilterRequest reservationFilterRequest) {
        ReservationDate dateFrom = ReservationDate.of(reservationFilterRequest.dateFrom());
        ReservationDate dateTo = ReservationDate.of(reservationFilterRequest.dateTo());
        return reservationRepository.findBy(reservationFilterRequest.memberId(), reservationFilterRequest.themeId(),
                dateFrom, dateTo).stream().map(ReservationResponse::new).toList();
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAllByStatus(ReservationStatus.RESERVED).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllWaitings() {
        return reservationRepository.findAllByStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
