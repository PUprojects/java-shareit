package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryInMemory implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    private long newUserId = 1;

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> getById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> getByEmail(String userEmail) {
        return Optional.ofNullable(usersByEmail.get(userEmail));
    }

    @Override
    public User create(User user) {
        user.setId(newUserId++);
        users.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
        return user;
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    @Override
    public void delete(User user) {
        usersByEmail.remove(user.getEmail());
        users.remove(user.getId());
    }
}
