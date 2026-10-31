package com.souplegit;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Soup PvP Legit - mod exclusivamente singleplayer.
 * clientSideOnly: o mod nem carrega em servidores dedicados.
 */
@Mod(modid = SoupLegitMod.MODID,
        name = SoupLegitMod.NAME,
        version = SoupLegitMod.VERSION,
        acceptedMinecraftVersions = "[1.8.9]",
        clientSideOnly = true)
public class SoupLegitMod {

    public static final String MODID = "souplegit";
    public static final String NAME = "Soup PvP Legit";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SoupConfig.load(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Cada handler e registrado nos dois barramentos do Forge 1.8.9
        // (eventos "gameevent" ficam no barramento da FML). Um barramento so
        // dispara os eventos que pertencem a ele, entao nao ha duplicacao.
        SoupHandler soupHandler = new SoupHandler();
        MinecraftForge.EVENT_BUS.register(soupHandler);
        FMLCommonHandler.instance().bus().register(soupHandler);

        ClientInputHandler inputHandler = new ClientInputHandler();
        MinecraftForge.EVENT_BUS.register(inputHandler);
        FMLCommonHandler.instance().bus().register(inputHandler);
    }
}
