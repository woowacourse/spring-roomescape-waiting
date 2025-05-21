package roomescape.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateWaitReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.global.ReservationStatus;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation addReservation(CreateReservationRequest request, LoginMemberRequest loginMemberRequest) {
        return createReservation(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId(),
                ReservationStatus.RESERVED);
    }

    public Reservation addReservationByAdmin(AdminCreateReservationRequest request) {
        return createReservation(request.memberId(), request.themeId(), request.date(), request.timeId(),
                ReservationStatus.RESERVED);
    }

    public Reservation addWaitReservation(CreateWaitReservationRequest request, LoginMemberRequest loginMemberRequest) {
        return createReservation(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId(),
                ReservationStatus.WAIT);
    }

    public void approveWaitReservationByAdmin(long waitReservationId) {
        Reservation waitReservation = reservationRepository.findById(waitReservationId)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약 대기입니다."));

        if (waitReservation.getStatus() == ReservationStatus.RESERVED) {
            throw new InvalidReservationException("이미 예약 처리 되었습니다.");
        }

        Optional<Reservation> cancelTargetOptional = reservationRepository.findByDateAndReservationTimeAndThemeAndStatus(
                waitReservation.getDate(),
                waitReservation.getReservationTime(),
                waitReservation.getTheme(),
                ReservationStatus.RESERVED);
        cancelTargetOptional.ifPresent(Reservation::cancel);

        waitReservation.changeStatusWaitToReserve();
    }

    private Reservation createReservation(long memberId,
                                          long themeId,
                                          LocalDate date,
                                          long timeId,
                                          ReservationStatus status) {
        Member member = memberRepository.findFetchById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        return member.reserve(date, reservationTime, theme, status);
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약 입니다."));
        reservation.cancel();
        confirmNextWaitingReservation(reservation);
    }

    private void confirmNextWaitingReservation(Reservation reservation) {
        List<Member> targetMembers = memberRepository.findNextReserveMember(reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId(),
                PageRequest.of(0, 1));
        if (targetMembers.isEmpty()) {
            return;
        }
        Member member = targetMembers.getFirst();
        member.waitToReserve(reservation.getDate(), reservation.getReservationTime(), reservation.getTheme());
    }

    public List<MyReservationResponse> findAllReservationOfMember(Long memberId) {
        Member member = memberRepository.findFetchById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 입니다."));
        List<MyReservationResponse> responses = new ArrayList<>();
        for (Reservation reservation : member.getReservations()) {
            checkStatusAndWaitRank(reservation, responses);
        }
        return responses;
    }

    private void checkStatusAndWaitRank(Reservation reservation, List<MyReservationResponse> responses) {
        if (reservation.getStatus() == ReservationStatus.WAIT) {
            Long waitRank = reservationRepository.countRankById(reservation.getId());
            responses.add(MyReservationResponse.of(reservation, waitRank));
            return;
        }
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            responses.add(MyReservationResponse.from(reservation));
        }
    }

    public List<Reservation> findAllByFilter(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationRepository.findAllByFilter(memberId, themeId, dateFrom, dateTo);
    }

    public List<ReservationWaitResponse> findAllByStatus(ReservationStatus status) {
        List<Reservation> waitReservations = reservationRepository.findAllByStatus(status);
        return waitReservations.stream()
                .map(ReservationWaitResponse::from)
                .toList();
    }
}
