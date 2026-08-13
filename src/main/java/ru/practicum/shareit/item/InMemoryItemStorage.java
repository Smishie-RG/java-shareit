package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Item add(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getByOwnerId(long ownerId) {
        List<Item> result = new ArrayList<>();

        for (Item item : items.values()) {
            if (item.getOwner().getId() == ownerId) {
                result.add(item);
            }
        }

        return result;
    }

    @Override
    public List<Item> search(String text) {
        List<Item> result = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        String searchText = text.toLowerCase(Locale.ROOT);

        for (Item item : items.values()) {
            boolean containsText =
                    item.getName()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchText)
                            || item.getDescription()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchText);

            if (Boolean.TRUE.equals(item.getAvailable()) && containsText) {
                result.add(item);
            }
        }

        return result;
    }
}