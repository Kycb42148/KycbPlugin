package kycb.plugin;

import arc.Events;
import arc.files.Fi;
import arc.util.*;
import arc.util.serialization.Json;
import arc.util.serialization.JsonWriter;
import kycb.plugin.util.Bundle;
import mindustry.Vars;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.*;
import mindustry.ui.Menus;

import java.util.HashSet;
import java.util.Locale;

public class KycbPlugin extends Plugin {

    private static int infoMenuId;
    private Config config;
    private Locale locale;

    private HashSet<String> votes = new HashSet<>();

    @Override
    public void init() {
        UpdateConfig();
        infoMenuId = Menus.registerMenu((player, option) -> {
            if (option != 0) return;
            Locale playerLocale = Bundle.findLocale(player.locale);
            Call.menu(player.con,-1, Bundle.get("game.info.title", playerLocale), Bundle.get("game.info.text", playerLocale), new String[][]{{Bundle.get("menu.close", playerLocale)}});
        });
        Events.on(PlayerJoin.class, event -> {
            Locale playerLocale = Bundle.findLocale(event.player.locale);
            Call.menu(event.player.con, infoMenuId, Bundle.get("game.rules.title", playerLocale), Bundle.get("game.rules.text", playerLocale), new String[][]{{ Bundle.get("menu.info", playerLocale), Bundle.get("menu.close", playerLocale) }});
        });
        Events.on(PlayerLeave.class, event -> {
            if (votes.contains(event.player.uuid())) {
                votes.remove(event.player.uuid());
                Call.sendMessage(Bundle.format("game.skip.leave", locale, event.player.name, votes.size(), (int) Math.ceil(config.skipRatio * Groups.player.size())));
            }
        });
        Log.info("Kycb plugin loaded.");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {

    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("skip", Bundle.get("commands.skip.usage"), Bundle.get("commands.skip.description"), (args, player) -> {
            if (!config.skip) return;
            votes.add(player.uuid());
            int required = (int) Math.ceil(config.skipRatio * Groups.player.size());
            Call.sendMessage(Bundle.format("game.skip.voted", locale, player.name, votes.size(), required));

            if (votes.size() >= required) {
                votes.clear();
                Call.sendMessage(Bundle.get("game.skip.changing", locale));
                Events.fire(new GameOverEvent(Team.crux));
            }
        });
    }

    private void UpdateConfig() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        Fi configFile = Vars.modDirectory.child("kycb/config.json");
        if (configFile.exists())
            config = json.fromJson(Config.class, configFile.readString());
        else configFile.writeString(json.toJson(config = new Config()));
        locale = Bundle.findLocale(config.locale);
    }
}
