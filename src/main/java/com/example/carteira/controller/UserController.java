
package com.example.carteira.controller;

import com.example.carteira.model.User;
import com.example.carteira.repository.UserRepository;
import com.example.carteira.service.TransferService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final TransferService transferService;

    public UserController(UserRepository userRepository, TransferService transferService) {
        this.userRepository = userRepository;
        this.transferService = transferService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam Long from,
                           @RequestParam Long to,
                           @RequestParam BigDecimal value) {
        transferService.transfer(from, to, value);
        return "OK";
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
