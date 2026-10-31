package com.souplegit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class SoupHandler {

    /**
     * Em singleplayer a sopa nao inicia o consumo vanilla (animacao de comer
     * por ~1,6s). Isso vale para cliente e servidor integrado, para que a
     * unica cura seja a da mecanica personalizada. Nao afeta outros itens.
     */
    @SubscribeEvent
    public void onUseStart(PlayerUseItemEvent.Start event) {
        ItemStack stack = event.item;
        if (stack == null || stack.getItem() != Items.mushroom_stew || !SoupConfig.enabled) {
            return;
        }
        EntityPlayer player = event.entityPlayer;
        if (player == null || !SingleplayerCheck.isActive(player.worldObj)) {
            return;
        }
        event.setCanceled(true);
    }

    /** Lado servidor: executa o pedido registrado pelo clique manual. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) {
            return;
        }
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (ClickGate.consume(player.getUniqueID())) {
            SoupLogic.tryUseSoup(player);
        }
    }
}
