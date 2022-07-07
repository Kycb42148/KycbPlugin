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
import java.util.Set;

public class KycbPlugin extends Plugin {

    private static int infoMenuId;
    private static Config config;

    private final Set<String> votes = new HashSet<>();

    @Override
    public void init() {
        updateConfig();
        infoMenuId = Menus.registerMenu((player, option) -> {
            if (option != 0) return;
            Locale playerLocale = Bundle.findLocale(player.locale);
            Call.menu(player.con, -1, Bundle.get("game.info.title", playerLocale), Bundle.get("game.info.text", playerLocale), new String[][]{{Bundle.get("menu.close", playerLocale)}});
        });
        Events.on(PlayerJoin.class, event -> {
            Locale playerLocale = Bundle.findLocale(event.player.locale);
            Call.menu(event.player.con, infoMenuId, Bundle.get("game.rules.title", playerLocale), Bundle.get("game.rules.text", playerLocale), new String[][]{{ Bundle.get("menu.info", playerLocale), Bundle.get("menu.close", playerLocale) }});
        });
        Events.on(PlayerLeave.class, event -> {
            if (votes.contains(event.player.uuid())) {
                votes.remove(event.player.uuid());
                sendBundled("game.skip.leave", event.player.name, votes.size(),
                        (int) Math.ceil(config.skipRatio * Groups.player.size()));
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
            sendBundled("game.skip.voted", player.name, votes.size(), required);

            if (votes.size() >= required) {
                votes.clear();
                sendBundled("game.skip.changing");
                Events.fire(new GameOverEvent(Team.crux));
            }
        });

        //TODO: Add "help" command with localisation

        handler.<Player>register("hub", Bundle.get("commands.hub.usage"), Bundle.get("commands.hub.description"), (args, player) -> {
            String[] address = config.hubAddress.split(":");
            Call.connect(player.con, address[0], (address[1] != null && Strings.canParseInt(address[1])) ? Integer.parseInt(address[1]): Vars.port);
        });
    }

    private void updateConfig() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        Fi configFile = Vars.modDirectory.child("kycb/config.json");
        if (configFile.exists())
            config = json.fromJson(Config.class, configFile.readString());
        else configFile.writeString(json.toJson(config = new Config()));
    }

    private static void sendBundled(String key, Object... format) {
        Groups.player.forEach(p -> {
            Locale locale = Bundle.findLocale(p.locale);
            p.sendMessage(Bundle.format(key, locale, format));
        });
    }
}
