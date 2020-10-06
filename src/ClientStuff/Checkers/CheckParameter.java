package ClientStuff.Checkers;

/**
 * Enum, содержащий в себе варианты получения информации от пользователя
 */
public enum CheckParameter{
    /**
     * Спрашивая значения полей, если они не удовлетворяют условиям
     */
    WITH_ASKING,
    /**
     * Не спрашивая значения полей, установить случайные/стандартные параметры
     */
    WHITHOUT_ASKING;
}
