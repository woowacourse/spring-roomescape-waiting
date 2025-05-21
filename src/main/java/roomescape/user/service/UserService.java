package roomescape.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.dto.ReservationWithStateDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.dto.UserRequestDto;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.user.exception.NotFoundUserException;
import roomescape.user.repository.UserRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository,
                       WaitingRepository waitingRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDto::of)
                .toList();
    }

    @Transactional
    public User add(UserRequestDto userRequestDto) {
        User user = userRequestDto.toEntity();
        return userRepository.save(user);
    }

    public List<ReservationWithStateDto> findAllReservationByMember(User member) {
        validateExistsUser(member.getId());

        List<Reservation> reservations = reservationRepository.findByUser(member);
        List<ReservationWithStateDto> dtos1 = convertReservationWithStateDto(reservations);

        List<Waiting> waitings = waitingRepository.findByMember(member);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(
                member.getId());
        List<ReservationWithStateDto> dtos2 = convertReservationWithStateDto(waitings, waitingWithRanks);

        dtos1.addAll(dtos2);
        return dtos1;
    }

    private static List<ReservationWithStateDto> convertReservationWithStateDto(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationWithStateDto::of)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<ReservationWithStateDto> convertReservationWithStateDto(List<Waiting> waitings, List<WaitingWithRank> waitingWithRanks) {
        List<ReservationWithStateDto> dtos = new ArrayList<>();
        for (Waiting waiting : waitings) {
            WaitingWithRank waitingWithRank = waitingWithRanks.stream()
                    .filter(o -> o.getWaiting().equals(waiting))
                    .findAny()
                    .get();
            dtos.add(ReservationWithStateDto.of(waitingWithRank));
        }
        return dtos;
    }

    @Transactional
    public void deleteReservationByMember(Long waitingId, User member) {
        validateExistsUser(member.getId());
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대기입니다."));

        if (!waiting.getMember().equals(member)) {
            throw new IllegalArgumentException("본인의 예약 대기만 삭제할 수 있습니다.");
        }

        waitingRepository.deleteById(waitingId);
    }

    private void validateExistsUser(Long id) {
        findByIdOrThrow(id);
    }

    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }
}
