package com.souplegit;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/** Configuracao: apenas ativar/desativar e quantidade de cura. */
public final class SoupConfig {

    public static final float DEFAULT_HEAL = 7.0F;

    public static volatile boolean enabled = true;
    public static volatile float healAmount = DEFAULT_HEAL;
    public static final float FOOD_SATURATION = 0.6F; // mesmo da sopa vanilla
    public static volatile int foodAmount = 6;

    private SoupConfig() {
    }

    public static void load(File file) {
        Configuration cfg = new Configuration(file);
        try {
            cfg.load();

            enabled = cfg.getBoolean("enabled", Configuration.CATEGORY_GENERAL, true,
                    "Ativa ou desativa a mecanica da sopa. A restricao ao singleplayer e fixa e nao pode ser desativada.");

            double heal = cfg.get(Configuration.CATEGORY_GENERAL, "healAmount", (double) DEFAULT_HEAL,
                    "Pontos de vida restaurados por sopa (2 pontos = 1 coracao). Padrao: 7 (3,5 coracoes). Aceita de 0.5 a 100.")
                    .getDouble((double) DEFAULT_HEAL);
            if (Double.isNaN(heal) || heal < 0.5D) {
                heal = 0.5D;
            } else if (heal > 100.0D) {
                heal = 100.0D;
            }
            healAmount = (float) heal;

            foodAmount = cfg.getInt("foodAmount", Configuration.CATEGORY_GENERAL, 6, 0, 20,
                    "Pontos de fome restaurados por sopa quando a vida ja esta cheia (2 pontos = 1 drumstick). Padrao: 6, igual a sopa vanilla.");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }
}
