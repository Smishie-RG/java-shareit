package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = getUser(userId);
        validateNewItem(itemDto);

        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(long userId,
                          long itemId,
                          ItemDto itemDto) {
        Item item = getItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Редактировать вещь может только владелец"
            );
        }

        if (itemDto.getName() != null) {
            validateText(
                    itemDto.getName(),
                    "Название не может быть пустым"
            );
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            validateText(
                    itemDto.getDescription(),
                    "Описание не может быть пустым"
            );
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getById(long userId, long itemId) {
        getUser(userId);
        Item item = getItem(itemId);
        boolean isOwner = item.getOwner().getId().equals(userId);
        return toItemResponseDto(item, isOwner);
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(long userId) {
        getUser(userId);
        List<ItemResponseDto> result = new ArrayList<>();
        for (Item item : itemRepository.findByOwnerId(userId)) {
            result.add(toItemResponseDto(item, true));
        }
        return result;
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        getUser(userId);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return toItemDtoList(itemRepository.search(text));
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId,
                                 long itemId,
                                 CommentDto commentDto) {
        User author = getUser(userId);
        Item item = getItem(itemId);
        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository
                .existsByItemIdAndBookerIdAndStatusAndEndBefore(
                        itemId,
                        userId,
                        BookingStatus.APPROVED,
                        now
                );

        if (!hasCompletedBooking) {
            throw new ValidationException(
                    "Отзыв можно оставить только после завершения бронирования"
            );
        }

        Comment comment = new Comment(
                null,
                commentDto.getText(),
                item,
                author,
                now
        );
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private List<ItemDto> toItemDtoList(List<Item> items) {
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            result.add(ItemMapper.toItemDto(item));
        }

        return result;
    }

    private ItemResponseDto toItemResponseDto(Item item,
                                               boolean includeBookings) {
        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        LocalDateTime now = LocalDateTime.now();

        if (includeBookings) {
            Booking last = bookingRepository
                    .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .orElse(null);
            Booking next = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .orElse(null);

            if (last != null) {
                lastBooking = new BookingShortDto(
                        last.getId(), last.getBooker().getId());
            }
            if (next != null) {
                nextBooking = new BookingShortDto(
                        next.getId(), next.getBooker().getId());
            }
        }

        List<CommentDto> comments = new ArrayList<>();
        for (Comment comment
                : commentRepository.findByItemIdOrderByCreatedDesc(item.getId())) {
            comments.add(CommentMapper.toCommentDto(comment));
        }

        return ItemMapper.toItemResponseDto(
                item,
                lastBooking,
                nextBooking,
                comments
        );
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

    private void validateNewItem(ItemDto itemDto) {
        validateText(
                itemDto.getName(),
                "Название не может быть пустым"
        );
        validateText(
                itemDto.getDescription(),
                "Описание не может быть пустым"
        );

        if (itemDto.getAvailable() == null) {
            throw new ValidationException(
                    "Статус доступности должен быть указан"
            );
        }
    }

    private void validateText(String text, String message) {
        if (text == null || text.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
