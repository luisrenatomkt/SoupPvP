package com.souplegit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Keyboard;

/**
 * Detecta o clique MANUAL (apertar o botao de "usar item") com sopa na mao e
 * registra no maximo um pedido por aperto. Nao gera cliques, nao mexe em
 * pacotes e nao faz nada se a sessao nao for singleplayer.
 */
public class ClientInputHandler {

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.button < 0 || !event.buttonstate) {
            return; // movimento do mouse ou botao sendo solto
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) {
            return;
        }
        // Botoes do mouse tem keyCode = botao - 100
        if (event.button - 100 != mc.gameSettings.keyBindUseItem.getKeyCode()) {
            return;
        }
        handlePress(mc);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) {
            return;
        }
        int use = mc.gameSettings.keyBindUseItem.getKeyCode();
        if (use <= 0) {
            return; // acao ligada a um botao do mouse (ou sem tecla)
        }
        if (!Keyboard.getEventKeyState() || Keyboard.isRepeatEvent() || Keyboard.getEventKey() != use) {
            return;
        }
        handlePress(mc);
    }

    /** Limpa pedidos pendentes ao sair do mundo. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && (mc.theWorld == null || mc.thePlayer == null)) {
            ClickGate.clear();
        }
    }

    private void handlePress(Minecraft mc) {
        if (!SoupConfig.enabled) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null || mc.currentScreen != null || !mc.inGameHasFocus) {
            return;
        }
        if (!SingleplayerCheck.isActive(mc.theWorld)) {
            return;
        }
        if (!player.isEntityAlive()) {
            return;
        }
        ItemStack held = player.getHeldItem();
        if (held == null || held.getItem() != Items.mushroom_stew) {
            return;
        }
        if (player.getHealth() >= player.getMaxHealth() && !player.getFoodStats().needFood()) {
            return; // vida e fome cheias: nao consome
        }
        if (clickGoesToTarget(mc, player)) {
            return; // o clique vai interagir com bloco/entidade (bau, porta, aldeao...)
        }
        ClickGate.register(player.getUniqueID());
    }

    private boolean clickGoesToTarget(Minecraft mc, EntityPlayerSP player) {
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit == null) {
            return false;
        }
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Entity e = mop.entityHit;
            return e instanceof EntityVillager || e instanceof EntityHorse || e instanceof EntityTameable
                    || e instanceof EntityMinecart || e instanceof EntityBoat
                    || e instanceof EntityItemFrame || e instanceof EntityArmorStand;
        }
        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (player.isSneaking()) {
                return false; // agachado com item na mao nao ativa blocos
            }
            Block block = mc.theWorld.getBlockState(mop.getBlockPos()).getBlock();
            return isInteractive(block);
        }
        return false;
    }

    private boolean isInteractive(Block b) {
        return b instanceof BlockContainer || b instanceof BlockWorkbench || b instanceof BlockAnvil
                || b instanceof BlockDoor || b instanceof BlockTrapDoor || b instanceof BlockFenceGate
                || b instanceof BlockBed || b instanceof BlockButton || b instanceof BlockLever
                || b instanceof BlockCake || b instanceof BlockRedstoneRepeater
                || b instanceof BlockRedstoneComparator;
    }
}
