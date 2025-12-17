package app.utils;

import app.gameStructures.ConfigData;

public class ConfigValidator {
    public static boolean validate(ConfigData data) {
        return (data.width() >= 10 && data.width() <= 100) &&
                (data.height() >= 10 && data.height() <= 100) &&
                (data.food_static() >= 0 && data.food_static() <= 100 && data.food_static() <= data.height() * data.width()) &&
                (data.state_delay_ms() >= 100 && data.state_delay_ms() <= 3000);
    }
}