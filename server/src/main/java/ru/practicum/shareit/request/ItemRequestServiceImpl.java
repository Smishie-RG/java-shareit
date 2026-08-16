package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto requestDto) {
        User requestor = getUser(userId);
        if (requestDto.getDescription() == null
                || requestDto.getDescription().isBlank()) {
            throw new ValidationException(
                    "Описание запроса не может быть пустым");
        }

        ItemRequest request = new ItemRequest(
                null,
                requestDto.getDescription(),
                requestor,
                LocalDateTime.now()
        );
        ItemRequest savedRequest = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(long userId) {
        getUser(userId);
        return toDtoList(
                requestRepository.findByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId, int from, int size) {
        getUser(userId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }
        PageRequest pageRequest = PageRequest.of(
                from / size,
                size,
                Sort.by(Sort.Direction.DESC, "created")
        );
        return toDtoList(requestRepository
                .findByRequestorIdNot(userId, pageRequest)
                .getContent());
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        getUser(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Запрос с id " + requestId + " не найден"));
        return ItemRequestMapper.toItemRequestDto(
                request,
                itemRepository.findByRequestId(requestId)
        );
    }

    private List<ItemRequestDto> toDtoList(List<ItemRequest> requests) {
        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest request : requests) {
            result.add(ItemRequestMapper.toItemRequestDto(
                    request,
                    itemRepository.findByRequestId(request.getId())
            ));
        }
        return result;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"));
    }
}
