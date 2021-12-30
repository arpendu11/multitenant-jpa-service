package com.stackabuse.multitenantjpaservice.service;

import com.stackabuse.multitenantjpaservice.dto.CreateUserDTO;
import com.stackabuse.multitenantjpaservice.dto.UpdateUserDTO;
import com.stackabuse.multitenantjpaservice.tenant.entity.User;

import java.util.List;

public interface UserService {

    List<User> getUsers();

    User getUser(String id);

    User createUser(CreateUserDTO createUserDTO);

    User updateUser(String id, UpdateUserDTO updateUserDTO) throws Exception;

    void deleteUserById(String id);
}
