package com.stackabuse.multitenantjpaservice.controller;

import com.stackabuse.multitenantjpaservice.dto.CreateUserDTO;
import com.stackabuse.multitenantjpaservice.dto.UpdateUserDTO;
import com.stackabuse.multitenantjpaservice.service.UserService;
import com.stackabuse.multitenantjpaservice.tenant.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUser(@PathVariable("userId") String userId) throws Exception {
        try {
            return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping(value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        User user = userService.createUser(createUserDTO);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.LOCATION, "/users/" + user.getId());
        return new ResponseEntity<>(user, headers, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> updateUser(@PathVariable String userId, @Valid @RequestBody UpdateUserDTO updateUserDTO)
            throws Exception {
        try {
            User user = userService.updateUser(userId, updateUserDTO);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable String userId) throws Exception {
        try {
            userService.deleteUserById(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping(value = "/async", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<List<User>>> asyncGetProducts() {
        List<User> users = userService.getUsers();
        return CompletableFuture.completedFuture(new ResponseEntity<>(users, HttpStatus.OK));
    }

    @GetMapping(value = "/async/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<User>> asyncGetProduct(@PathVariable("userId") String userId)
            throws Exception {
        return CompletableFuture.completedFuture(getUser(userId));
    }
}
