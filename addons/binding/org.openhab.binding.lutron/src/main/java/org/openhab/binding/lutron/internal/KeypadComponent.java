package org.openhab.binding.lutron.internal;

public interface KeypadComponent {

    // void COMPONENT(final int i, final String c);

    public int id();

    public String channel();

    public static boolean isLed(int id) {
        return (id >= 81 && id <= 95);
    }

    public static boolean isButton(int id) {
        return (id >= 1 && id <= 25);
    }

    public static boolean isCCI(int id) {
        return (id >= 1 && id <= 25);
    }

}
