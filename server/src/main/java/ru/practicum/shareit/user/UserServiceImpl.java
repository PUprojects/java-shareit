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
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getById(long userId) {
        return userMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Поользователь " + userId + " не найден")));
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateUniqueUser(null, userDto.email());
        return userMapper.toUserDto(
                userRepository.save(
                        userMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = getUserById(userId);
        validateUniqueUser(userId, userDto.email());
        user.setId(userId);
        if (userDto.name() != null) {
            user.setName(userDto.name());
        }
        if (userDto.email() != null) {
            user.setEmail(userDto.email());
        }
        userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    public void delete(long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private void validateUniqueUser(Long id, String email) {
        if (email == null) {
            return;
        }

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
