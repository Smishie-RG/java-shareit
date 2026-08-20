package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDto create(long userId, NewBookingDto bookingDto) {
        User booker = getUser(userId);
        Item item = getItem(bookingDto.getItemId());

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(long userId,
                              long bookingId,
                              boolean approved) {
        Booking booking = getBooking(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException(
                    "Подтвердить бронирование может только владелец вещи"
            );
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменён");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(long userId, long bookingId) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Нет доступа к бронированию");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(long userId, BookingState state,
                                        int from, int size) {
        getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = createPageRequest(from, size);
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository
                        .findCurrentByBookerId(userId, now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository
                        .findByBookerIdAndEndBeforeOrderByStartDesc(
                                userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findByBookerIdAndStartAfterOrderByStartDesc(
                                userId, now, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.REJECTED, pageRequest);
                break;
            default:
                bookings = bookingRepository
                        .findByBookerIdOrderByStartDesc(userId, pageRequest);
        }

        return toBookingDtoList(bookings);
    }

    @Override
    public List<BookingDto> getByOwner(long userId, BookingState state,
                                       int from, int size) {
        getUser(userId);
        if (!itemRepository.existsByOwnerId(userId)) {
            throw new NotFoundException("У пользователя нет вещей");
        }

        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = createPageRequest(from, size);
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository
                        .findCurrentByOwnerId(userId, now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository
                        .findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                                userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findByItemOwnerIdAndStartAfterOrderByStartDesc(
                                userId, now, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findByItemOwnerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findByItemOwnerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.REJECTED, pageRequest);
                break;
            default:
                bookings = bookingRepository
                        .findByItemOwnerIdOrderByStartDesc(userId, pageRequest);
        }

        return toBookingDtoList(bookings);
    }

    private List<BookingDto> toBookingDtoList(List<Booking> bookings) {
        List<BookingDto> result = new ArrayList<>();
        for (Booking booking : bookings) {
            result.add(BookingMapper.toBookingDto(booking));
        }
        return result;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"
                ));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id " + itemId + " не найдена"
                ));
    }

    private Booking getBooking(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        "Бронирование с id " + bookingId + " не найдено"
                ));
    }

    private PageRequest createPageRequest(int from, int size) {
        return PageRequest.of(from / size, size);
    }
}
