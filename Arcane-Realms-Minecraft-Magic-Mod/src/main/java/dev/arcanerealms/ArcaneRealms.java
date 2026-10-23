package dev.arcanerealms;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class ArcaneRealms implements ModInitializer {
    public static final String MOD_ID = "arcanerealms";

    public static final StaffItem ASTRAL_STAFF = register("astral_staff", StaffItem.Element.ASTRAL);
    public static final StaffItem EMBER_STAFF = register("ember_staff", StaffItem.Element.EMBER);
    public static final StaffItem FROST_STAFF = register("frost_staff", StaffItem.Element.FROST);

    private static StaffItem register(String path, StaffItem.Element element) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        StaffItem staff = new StaffItem(element, new Item.Properties()
                .setId(key).stacksTo(1).rarity(Rarity.EPIC));
        return Registry.register(BuiltInRegistries.ITEM, key, staff);
    }

    @Override
    public void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> {
                    tab.accept(ASTRAL_STAFF);
                    tab.accept(EMBER_STAFF);
                    tab.accept(FROST_STAFF);
                });
        ServerTickEvents.END_SERVER_TICK.register(ArcaneRealms::tick);
    }

    private static void tick(MinecraftServer server) {
        // HUD + ambient effects update at 5 Hz; regen updates continuously.
        int tick = server.getTickCount();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ManaManager.regenerate(player, tick);
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof StaffItem staff) {
                if (tick % 10 == 0) {
                    SpellEffects.ambient(player.level(), player, staff.element(), tick);
                }
                if (tick % 20 == 0) {
                    staff.showStatus(player, held);
                }
            }
        }
        if (tick % 1200 == 0) {
            ManaManager.cleanup(server.getPlayerList().getPlayers());
        }
    }
}
