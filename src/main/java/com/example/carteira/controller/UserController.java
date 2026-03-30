
package com.example.carteira.controller;

import com.example.carteira.model.User;
import com.example.carteira.model.User.PublicView;
import com.example.carteira.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    public UserController(  UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public User create(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    @JsonView(PublicView.class)
    public User get(@PathVariable Long id) {
        return userService.findById(id);
    }
}
