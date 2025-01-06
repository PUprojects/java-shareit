package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getById(long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Поользователь " + userId + " не найден")));
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateUniqueUser(null, userDto.getEmail());
        return UserMapper.toUserDto(
                userRepository.save(
                        UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = getUserById(userId);
        validateUniqueUser(userId, userDto.getEmail());
        user.setId(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(long userId) {
        User user = UserMapper.toUser(getById(userId));
        userRepository.delete(user);
    }

    private void validateUniqueUser(Long id, String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if ((id == null) || (id != user.getId())) {
                throw new AlreadyExistException("Пользователь с почтой " + email + " уже существует");
            }
        });
    }

    public User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Поользователь " + userId + " не найден"));
    }
}
