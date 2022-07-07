package kycb.plugin.util;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.gen.Player;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bundle {

    private static final ObjectMap<Locale, ResourceBundle> bundles = new ObjectMap<>();
    private static final ObjectMap<Locale, MessageFormat> formats = new ObjectMap<>();
    private static final Locale[] supportedLocales;

    public static final Locale defaultLocale = new Locale("en");

    static {
        Fi[] files = Vars.mods.getMod("kycbplugin").root.child("bundles").list();
        supportedLocales = new Locale[files.length];

        for (int i = 0; i < files.length; i++) {
            String code = files[i].nameWithoutExtension();

            String[] codes;
            if (!code.contains("_")) { // bundle.properties
                supportedLocales[i] = Locale.ROOT;
            } else if ((codes = code.split("_")).length == 3) { // bundle_uk_UA.properties
                supportedLocales[i] = new Locale(codes[1], codes[2]);
            } else { // bundle_ru.properties
                supportedLocales[i] = new Locale(codes[1]);
            }
        }
    }

    private Bundle() {
    }

    public static String get(String key, Locale locale) {
        try {
            ResourceBundle bundle = getOrLoad(locale);

            return bundle.getString(key);
        } catch (MissingResourceException t) {
            Log.err("Unknown key '@', locale: @", key, locale);
            return "???" + key + "???";
        }
    }

    public static String get(String key) {
        return get(key, defaultLocale);
    }

    public static String format(String key, Locale locale, Object... values) {
        String pattern = get(key, locale);
        MessageFormat format = formats.get(locale);
        if (!Structs.contains(supportedLocales, locale)) {
            format = formats.get(defaultLocale, () -> new MessageFormat(pattern, defaultLocale));
            format.applyPattern(pattern);
        } else if (format == null) {
            formats.put(locale, format = new MessageFormat(pattern, locale));
        } else {
            format.applyPattern(pattern);
        }
        return format.format(values);
    }

    public static String format(String key, Object... values) {
        return format(key, defaultLocale, values);
    }

    private static ResourceBundle getOrLoad(Locale locale) {
        ResourceBundle bundle = bundles.get(locale);
        if (bundle == null) {
            if (Structs.contains(supportedLocales, locale)) {
                bundles.put(locale, bundle = ResourceBundle.getBundle("bundles.bundle", locale));
            } else {
                bundle = getOrLoad(defaultLocale);
            }
        }
        return bundle;
    }

    public static Locale findLocale(Player player) {
        Locale locale = Structs.find(supportedLocales, l -> player.locale.equals(l.toString()) || player.locale.startsWith(l.toString()));
        return locale != null ? locale : defaultLocale;
    }
}
