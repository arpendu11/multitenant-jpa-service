package com.stackabuse.multitenantjpaservice.service;

import com.stackabuse.multitenantjpaservice.dto.CreateUserDTO;
import com.stackabuse.multitenantjpaservice.dto.UpdateUserDTO;
import com.stackabuse.multitenantjpaservice.tenant.entity.User;
import com.stackabuse.multitenantjpaservice.tenant.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(String id) {
        return userRepository.findById(UUID.fromString(id)).get();
    }

    @Override
    @Transactional
    public User createUser(CreateUserDTO createUserDTO) {
        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());
        user.setCreatedBy("admin");
        user.setCreatedOn(System.currentTimeMillis());
        user.setLastUpdatedBy("admin");
        user.setLastUpdatedOn(System.currentTimeMillis());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(String id, UpdateUserDTO updateUserDTO) throws Exception {
        User user = userRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + id + " not found in the tenant"));
        user.setFirstName(updateUserDTO.getFirstName());
        user.setLastName(updateUserDTO.getLastName());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserById(String id) {
        User user = userRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + id + " not found in the tenant"));
        userRepository.delete(user);
    }
}
