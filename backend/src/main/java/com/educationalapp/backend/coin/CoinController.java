package com.educationalapp.backend.coin;

import com.educationalapp.backend.coin.CoinDtos.CoinBalanceResponse;
import com.educationalapp.backend.coin.CoinDtos.CoinTransactionResponse;
import com.educationalapp.backend.user.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coins")
public class CoinController {
    private final UserService userService;
    private final CoinTransactionRepository coinTransactionRepository;

    public CoinController(UserService userService, CoinTransactionRepository coinTransactionRepository) {
        this.userService = userService;
        this.coinTransactionRepository = coinTransactionRepository;
    }

    @GetMapping("/user/{userId}/balance")
    public CoinBalanceResponse balance(@PathVariable Long userId) {
        return new CoinBalanceResponse(userId, userService.getUser(userId).getCoins());
    }

    @GetMapping("/user/{userId}/transactions")
    public List<CoinTransactionResponse> transactions(@PathVariable Long userId) {
        return coinTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CoinTransactionResponse::from)
                .toList();
    }
}
