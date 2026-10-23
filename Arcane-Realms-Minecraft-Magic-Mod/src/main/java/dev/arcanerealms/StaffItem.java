package dev.arcanerealms;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** Right-click = cast; Shift + right-click = switch spell. Spell index survives saves via custom_data. */
public final class StaffItem extends Item {
    public enum Element {
        ASTRAL("Astral", ChatFormatting.LIGHT_PURPLE, 0xAA65FF,
                new String[] {"Starstep", "Astral Mend", "Cosmic Halo", "Starlight Sight"},
                new int[] {28, 32, 35, 18}, new int[] {30, 70, 90, 45}),
        EMBER("Ember", ChatFormatting.GOLD, 0xFF8A25,
                new String[] {"Phoenix Leap", "Flameward", "Solar Flare", "Blazing Stride"},
                new int[] {24, 26, 30, 22}, new int[] {40, 80, 65, 55}),
        FROST("Frost", ChatFormatting.AQUA, 0x55DFFF,
                new String[] {"Frost Bridge", "Snow Globe", "Winter Ward", "Crystal Rush"},
                new int[] {30, 28, 26, 22}, new int[] {65, 70, 80, 45});

        public final String name;
        public final ChatFormatting color;
        public final int rgb;
        public final String[] spells;
        public final int[] manaCosts;
        public final int[] cooldowns;

        Element(String name, ChatFormatting color, int rgb, String[] spells, int[] manaCosts, int[] cooldowns) {
            this.name = name;
            this.color = color;
            this.rgb = rgb;
            this.spells = spells;
            this.manaCosts = manaCosts;
            this.cooldowns = cooldowns;
        }
    }

    private final Element element;

    public StaffItem(Element element, Properties properties) {
        super(properties);
        this.element = element;
    }

    public Element element() {
        return element;
    }

    public int selectedSpell(ItemStack stack) {
        int saved = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getIntOr("arcane_spell", 0);
        return Math.floorMod(saved, element.spells.length);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            int next = (selectedSpell(stack) + 1) % element.spells.length;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("arcane_spell", next));
            player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.65F, 0.85F + (next * 0.15F));
            showStatus(player, stack);
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            player.sendOverlayMessage(Component.literal("✦ Staff is recharging…").withStyle(element.color));
            return InteractionResult.FAIL;
        }
        int selected = selectedSpell(stack);
        if (!ManaManager.spend(player, element.manaCosts[selected])) {
            player.sendOverlayMessage(Component.literal("✦ Not enough mana — " + ManaManager.get(player) + "/100")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        SpellEffects.cast(player.level(), player, element, selected);
        player.getCooldowns().addCooldown(stack, element.cooldowns[selected]);
        player.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8F, 0.8F + selected * 0.12F);
        showStatus(player, stack);
        return InteractionResult.SUCCESS;
    }

    public void showStatus(ServerPlayer player, ItemStack stack) {
        int selected = selectedSpell(stack);
        String text = "✦ " + element.name + " · " + element.spells[selected]
                + "  [" + ManaManager.get(player) + "/" + ManaManager.MAX + " mana]"
                + "  · Shift+RMB: change";
        player.sendOverlayMessage(Component.literal(text).withStyle(element.color));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        int selected = selectedSpell(stack);
        tooltip.accept(Component.literal("✦ " + element.name + " magic · 4 spells").withStyle(element.color));
        tooltip.accept(Component.literal("Right-click to cast • Sneak + right-click to switch")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Selected: " + element.spells[selected]
                + " · " + element.manaCosts[selected] + " mana").withStyle(ChatFormatting.WHITE));
        tooltip.accept(Component.literal("Mana regenerates automatically").withStyle(ChatFormatting.DARK_GRAY));
    }
}
