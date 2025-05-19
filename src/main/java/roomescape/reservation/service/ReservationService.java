package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.dto.response.ReservationAdminCreateResponse;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationReadFilteredResponse;
import roomescape.reservation.dto.response.ReservationReadResponse;
import roomescape.reservation.dto.response.ReservationWaitingReadMemberResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationCreateResponse createReservation(Long memberId, ReservationCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation saved = reservationRepository.save(newReservation);
        return ReservationCreateResponse.from(saved, theme);
    }

    public ReservationAdminCreateResponse createReservationByAdmin(ReservationAdminCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Reservation newReservation = new Reservation(request.date(), time, theme, member);
        validateDateTime(newReservation);
        validateDuplicated(newReservation);

        Reservation reservation = reservationRepository.save(newReservation);
        return ReservationAdminCreateResponse.from(reservation, theme);
    }

    public List<ReservationReadResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationReadResponse::from)
                .toList();
    }

    public List<ReservationReadFilteredResponse> getFilteredReservations(ReservationReadFilteredRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndMemberIdAndDateBetween(
                request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
        );

        return reservations.stream()
                .map(ReservationReadFilteredResponse::from)
                .toList();
    }

    public List<ReservationWaitingReadMemberResponse> getReservationsByMember(LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));

        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(loginMember.id());

        return ReservationWaitingReadMemberResponse.of(reservations, waitingWithRanks);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private void validateDateTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = reservation.getDateTime();
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicated(Reservation reservation) {
        boolean hasDuplicate = reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if (hasDuplicate) {
            throw new ConflictException("이미 예약이 존재합니다.");
        }
    }
}
