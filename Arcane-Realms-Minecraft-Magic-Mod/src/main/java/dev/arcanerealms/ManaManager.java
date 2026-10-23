package dev.arcanerealms;

import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Server-authoritative mana pool; replenishes automatically. */
public final class ManaManager {
    public static final int MAX = 100;
    private static final Map<UUID, Integer> MANA = new HashMap<>();

    private ManaManager() {}

    public static int get(ServerPlayer player) {
        return MANA.getOrDefault(player.getUUID(), MAX);
    }

    public static boolean spend(ServerPlayer player, int cost) {
        int current = get(player);
        if (current < cost) return false;
        MANA.put(player.getUUID(), current - cost);
        return true;
    }

    public static void regenerate(ServerPlayer player, int serverTick) {
        if (serverTick % 4 == 0 && get(player) < MAX) {
            MANA.put(player.getUUID(), get(player) + 1);
        }
    }

    public static void cleanup(List<ServerPlayer> onlinePlayers) {
        Set<UUID> online = onlinePlayers.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet());
        MANA.keySet().retainAll(online);
    }
}
