package com.souplegit;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

/**
 * Regra de cura e consumo. Executa somente no servidor integrado.
 * - Vida faltando: cura (padrao 7 HP) e NAO mexe na fome.
 * - Vida cheia e com fome: recupera fome (padrao 6, como a sopa vanilla).
 * - Vida e fome cheias: a sopa nao e consumida.
 */
public final class SoupLogic {

    private SoupLogic() {
    }

    /** @return true se uma sopa foi consumida. */
    public static boolean tryUseSoup(EntityPlayerMP player) {
        if (!SoupConfig.enabled || !SingleplayerCheck.isActive(player.worldObj)) {
            return false;
        }
        if (player.isDead || player.getHealth() <= 0.0F || player.isSpectator()
                || player.capabilities.isCreativeMode) {
            return false;
        }

        InventoryPlayer inv = player.inventory;
        ItemStack held = inv.getCurrentItem();
        if (held == null || held.getItem() != Items.mushroom_stew || held.stackSize <= 0) {
            return false; // so sopa de cogumelos
        }

        float missing = player.getMaxHealth() - player.getHealth();
        boolean heal = missing > 0.0F;
        boolean feed = !heal && player.getFoodStats().needFood();
        if (!heal && !feed) {
            return false; // nada a recuperar: preserva a sopa
        }

        // 1) Consome a sopa e devolve a tigela (antes do efeito, para nunca dar efeito sem consumir).
        if (held.stackSize == 1) {
            inv.setInventorySlotContents(inv.currentItem, new ItemStack(Items.bowl));
        } else {
            held.stackSize--;
            if (!inv.addItemStackToInventory(new ItemStack(Items.bowl))) {
                held.stackSize++; // desfaz: nada e perdido
                player.addChatMessage(new ChatComponentText(
                        "[Soup PvP Legit] Sem espaco para a tigela vazia; a sopa nao foi consumida."));
                return false;
            }
        }

        // 2) Efeito: cura OU fome, nunca os dois no mesmo uso.
        if (heal) {
            player.heal(Math.min(SoupConfig.healAmount, missing));
        } else {
            player.getFoodStats().addStats(SoupConfig.foodAmount, SoupConfig.FOOD_SATURATION);
        }

        player.inventoryContainer.detectAndSendChanges();
        return true;
    }
}
