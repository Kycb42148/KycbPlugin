package kycb.plugin.util;

import arc.files.Fi;
import arc.util.Log;
import arc.util.io.Streams;
import mindustry.Vars;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ResourceUtil {
    public static void saveFileFromResources(String fileName, String destination) {
        Fi destinationFile = Vars.modDirectory.child(destination);
        if (!destinationFile.exists()) {
            try {
                InputStream stream = ResourceUtil.class.getClassLoader().getResourceAsStream(fileName);
                Objects.requireNonNull(stream, "stream");
                Streams.copy(stream, destinationFile.write(false));
            } catch (IOException | NullPointerException e) {
                Log.err(e);
            }
        }
    }
}
