
package com.example.carteira.controller;

import com.example.carteira.model.User;
import com.example.carteira.model.User.PublicView;
import com.example.carteira.service.TransferService;
import com.example.carteira.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/users")
public class UserController {

    private final TransferService transferService;
    private final UserService userService;


    public UserController( TransferService transferService, UserService userService) {
        this.userService = userService;
        this.transferService = transferService;
    }

    @PostMapping("/create")
    public User create(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam Long from,
                           @RequestParam Long to,
                           @RequestParam BigDecimal value) {
        transferService.transfer(from, to, value);
        return "OK";
    }

    @GetMapping("/{id}")
    @JsonView(PublicView.class)
    public User get(@PathVariable Long id) {
        return userService.findById(id);
    }
}
