
package com.example.carteira.controller;

import com.example.carteira.service.TransferService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController( TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public String transfer(@RequestParam Long from,
                           @RequestParam Long to,
                           @RequestParam BigDecimal value) {
        transferService.transfer(from, to, value);
        return "OK";
    }
}
