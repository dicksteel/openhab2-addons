/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import java.util.Arrays;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron Pico keypads
 *
 * @author Bob Adair - Initial contribution
 */
public class PicoKeypadHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        // Buttons for 2B, 2BRL, 3B, and 3BRL models
        BUTTON1(2, "button1", "Button 1"),
        BUTTON2(3, "button2", "Button 2"),
        BUTTON3(4, "button3", "Button 3"),

        RAISE(5, "buttonraise", "Raise Button"),
        LOWER(6, "buttonlower", "Lower Button"),

        // Buttons for PJ2-4B model
        BUTTON01(8, "button01", "Button 1"),
        BUTTON02(9, "button02", "Button 2"),
        BUTTON03(10, "button03", "Button 3"),
        BUTTON04(11, "button04", "Button 4");

        private final int id;
        private final String channel;
        private final String description;

        COMPONENT(final int i, final String c, final String d) {
            id = i;
            channel = c;
            description = d;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public String channel() {
            return this.channel;
        }

        @Override
        public String description() {
            return this.description;
        }
    }

    private Logger logger = LoggerFactory.getLogger(PicoKeypadHandler.class);

    public PicoKeypadHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean isLed(int id) {
        return false; // No LEDs on Picos
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 2 && id <= 11);
    }

    @Override
    protected boolean isCCI(int id) {
        return false;
    }

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", mod);

        switch (mod) {
            case "2B":
                buttonList = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON3);
                break;
            case "2BRL":
                buttonList = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON3, COMPONENT.RAISE, COMPONENT.LOWER);
                break;
            case "3B":
                buttonList = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3);
                break;
            case "4B":
                buttonList = Arrays.asList(COMPONENT.BUTTON01, COMPONENT.BUTTON02, COMPONENT.BUTTON03,
                        COMPONENT.BUTTON04);
                break;
            default:
                this.logger.warn("No valid keypad model defined ({}). Assuming model 3BRL.", mod);
            case "3BRL":
                buttonList = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3, COMPONENT.RAISE,
                        COMPONENT.LOWER);
                break;
        }
    }

}
