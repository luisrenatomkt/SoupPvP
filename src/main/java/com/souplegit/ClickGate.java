package com.souplegit;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ponte cliente -> servidor integrado (mesma JVM).
 * O cliente registra UM pedido por clique fisico (borda de descida do botao);
 * o servidor consome o pedido uma unica vez. Segurar o botao nao gera novos
 * pedidos, e pedidos velhos expiram.
 */
public final class ClickGate {

    private static final long MAX_AGE_NANOS = 1000000000L; // 1 segundo
    private static final ConcurrentHashMap<UUID, Long> PENDING = new ConcurrentHashMap<UUID, Long>();

    private ClickGate() {
    }

    public static void register(UUID player) {
        long now = System.nanoTime();
        Long old = PENDING.get(player);
        if (old != null && now - old.longValue() <= MAX_AGE_NANOS) {
            return; // ja existe um pedido valido; nao empilha
        }
        PENDING.put(player, Long.valueOf(now));
    }

    /** @return true se havia um pedido valido (e ele foi removido). */
    public static boolean consume(UUID player) {
        Long t = PENDING.remove(player);
        return t != null && System.nanoTime() - t.longValue() <= MAX_AGE_NANOS;
    }

    public static void clear() {
        PENDING.clear();
    }
}
