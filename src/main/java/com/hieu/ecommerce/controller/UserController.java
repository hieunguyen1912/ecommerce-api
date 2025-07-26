package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String getAllUsers() {
        // This method will return all users
        return "List of all users";
    }

    @GetMapping("/id")
    public String getUserById(@PathVariable Long id) {
        // This method will return a user by ID
        return "User with ID: " + id;
    }

    @PutMapping("/id")
    public String updateUser(@PathVariable Long id) {
        return "";
    }

    @DeleteMapping("/id")
    public String deleteUser(@PathVariable Long id) {
        return "";
    }
}
