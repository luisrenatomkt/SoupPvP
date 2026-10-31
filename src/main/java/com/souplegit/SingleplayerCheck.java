package com.souplegit;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;

/**
 * Verificacao de sessao singleplayer. Nao e configuravel.
 * Cliente: precisa haver um servidor integrado rodando.
 * Servidor: a instancia precisa ser IntegratedServer (nunca um dedicado).
 */
public final class SingleplayerCheck {

    private SingleplayerCheck() {
    }

    public static boolean isActive(World world) {
        if (world == null) {
            return false;
        }
        if (world.isRemote) {
            Minecraft mc = Minecraft.getMinecraft();
            return mc != null && mc.isSingleplayer() && mc.getIntegratedServer() != null;
        }
        MinecraftServer server = MinecraftServer.getServer();
        return server instanceof IntegratedServer && !server.isDedicatedServer();
    }
}
