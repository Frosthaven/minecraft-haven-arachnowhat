package dev.frosthaven.havenarachnowhat;

import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

/** The mod's log in latest.log, prefixed "[arachnowhat]". Nothing per frame: {@link #once} logs a key the first time,
 *  {@link #error} once per key with the stack trace. */
public final class Log {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String PREFIX = "[arachnowhat] ";
    private final Map<String, Boolean> seen = new ConcurrentHashMap<>();

    public void once(String key, String msg, Object... args) {
        if (seen.putIfAbsent("once:" + key, Boolean.TRUE) == null) LOG.info(PREFIX + msg, args);
    }

    public void error(String key, String msg, Throwable t) {
        if (seen.putIfAbsent("err:" + key, Boolean.TRUE) == null) LOG.error(PREFIX + msg, t);
    }
}
