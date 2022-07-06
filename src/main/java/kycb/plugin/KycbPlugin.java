package kycb.plugin;

import arc.Events;
import arc.util.*;
import kycb.plugin.util.Bundle;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.*;
import mindustry.ui.Menus;

import java.util.Locale;

public class KycbPlugin extends Plugin {

    public static int infoMenuId;

    @Override
    public void init() {
        infoMenuId = Menus.registerMenu((player, option) -> {
            if (option != 0) return;
            Locale playerLocale = Bundle.findLocale(player);
            Call.menu(player.con,-1, Bundle.get("game.info.title", playerLocale), Bundle.get("game.info.text", playerLocale), new String[][]{{Bundle.get("menu.close", playerLocale)}});
        });
        Events.on(PlayerJoin.class, event -> {
            Locale playerLocale = Bundle.findLocale(event.player);
            Call.menu(event.player.con, infoMenuId, Bundle.get("game.rules.title", playerLocale), Bundle.get("game.rules.text", playerLocale), new String[][]{{ Bundle.get("menu.info", playerLocale), Bundle.get("menu.close", playerLocale) }});
        });
        Log.info("Kycb plugin loaded.");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {

    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("skip", Bundle.get("commands.skip.usage"), Bundle.get("commands.skip.description"), (args, player) -> {

        });
    }
}
