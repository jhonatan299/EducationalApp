package com.educationalapp.backend.coin;

import java.time.LocalDateTime;

public final class CoinDtos {
    private CoinDtos() {
    }

    public record CoinBalanceResponse(Long userId, int coins) {
    }

    public record CoinTransactionResponse(Long id, Long userId, int amount, String reason, LocalDateTime createdAt) {
        public static CoinTransactionResponse from(CoinTransaction transaction) {
            return new CoinTransactionResponse(
                    transaction.getId(),
                    transaction.getUserId(),
                    transaction.getAmount(),
                    transaction.getReason(),
                    transaction.getCreatedAt()
            );
        }
    }
}
