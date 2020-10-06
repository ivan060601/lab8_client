package Application.Locale;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localizator {
    public static final Locale RUSSIA_RUSSIA = new Locale("ru", "RU");
    public static final Locale CROATIAN_CROATIA = new Locale("hr", "HR");
    public static final Locale CZECH_CZECH = new Locale("cz", "CZ");
    public static final Locale ENGLISH_CANADA = new Locale("en", "CA");
    public static final Locale DEFAULT = Locale.getDefault();

    public static ResourceBundle changeLocale(String bundleName, Locale newLocale) {
        ResourceBundle resources = null;
        try {
            resources = ResourceBundle.getBundle(bundleName, newLocale);
            return resources;
        } catch (MissingResourceException exception) {
            return null;
        }
    }
}
