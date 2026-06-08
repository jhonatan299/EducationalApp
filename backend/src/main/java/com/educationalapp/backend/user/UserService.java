package com.educationalapp.backend.user;

import com.educationalapp.backend.coin.CoinTransaction;
import com.educationalapp.backend.coin.CoinTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    public UserService(AppUserRepository userRepository, CoinTransactionRepository coinTransactionRepository) {
        this.userRepository = userRepository;
        this.coinTransactionRepository = coinTransactionRepository;
    }

    public AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional
    public AppUser addCoins(Long userId, int amount, String reason) {
        AppUser user = getUser(userId);
        user.addCoins(amount);
        coinTransactionRepository.save(new CoinTransaction(userId, amount, reason));
        return user;
    }
}
