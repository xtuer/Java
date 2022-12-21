package util;

import com.jcraft.jsch.JSch;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Demonstrates enabling and routing JSch (http://www.jcraft.com/jsch/) logging
 * into SLF4J (http://www.slf4j.org/) logging using Java 8
 */
@Slf4j
public class JschLogger {
    public JschLogger() {
        JSch.setLogger(new com.jcraft.jsch.Logger() {
            private final HashMap<Integer, Consumer<String>> logMap = new HashMap<>();
            private final HashMap<Integer, BooleanSupplier> enabledMap = new HashMap<>();

            {
                logMap.put(com.jcraft.jsch.Logger.DEBUG, log::debug);
                logMap.put(com.jcraft.jsch.Logger.ERROR, log::error);
                logMap.put(com.jcraft.jsch.Logger.FATAL, log::error);
                logMap.put(com.jcraft.jsch.Logger.INFO, log::info);
                logMap.put(com.jcraft.jsch.Logger.WARN, log::warn);

                enabledMap.put(com.jcraft.jsch.Logger.DEBUG, log::isDebugEnabled);
                enabledMap.put(com.jcraft.jsch.Logger.ERROR, log::isErrorEnabled);
                enabledMap.put(com.jcraft.jsch.Logger.FATAL, log::isErrorEnabled);
                enabledMap.put(com.jcraft.jsch.Logger.INFO, log::isInfoEnabled);
                enabledMap.put(com.jcraft.jsch.Logger.WARN, log::isWarnEnabled);
            }

            @Override
            public void log(int level, String message) {
                logMap.get(level).accept(message);
            }

            @Override
            public boolean isEnabled(int level) {
                return enabledMap.get(level).getAsBoolean();
            }
        });
    }
}
