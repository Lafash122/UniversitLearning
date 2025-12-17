package app.gameStructures;

import java.util.Properties;

public record ConfigData(int width, int height, int food_static, int state_delay_ms) {
    public static ConfigData parse(Properties properties) {
        ConfigData configData = null;
        try {
            configData = new ConfigData(Integer.parseInt(properties.getProperty("width")),
                                        Integer.parseInt(properties.getProperty("height")),
                                        Integer.parseInt(properties.getProperty("food_static")),
                                        Integer.parseInt(properties.getProperty("state_delay_ms")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return configData;
    }
}