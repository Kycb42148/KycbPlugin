package kycb.plugin;

import arc.Events;
import arc.util.*;
import kycb.plugin.util.Bundle;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.mod.*;

public class KycbPlugin extends Plugin {

    @Override
    public void init() {
        Events.on(PlayerJoin.class, event -> {
            Call.menu(event.player.con, 0, Bundle.get("game.rules.title", Bundle.findLocale(event.player)), Bundle.get("game.rules.text", Bundle.findLocale(event.player)), new String[][]{{Bundle.get("menu.close", Bundle.findLocale(event.player))}});
        });
        Log.info("Kycb plugin loaded.");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {

    }

    @Override
    public void registerClientCommands(CommandHandler handler) {

    }
}
