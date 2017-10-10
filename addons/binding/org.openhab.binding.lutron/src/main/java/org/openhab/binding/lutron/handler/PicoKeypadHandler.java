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
 * Handler responsible for communicating with Lutron Tabletop seeTouch keypads used in RadioRA2 and Homeworks QS systems
 * (e.g. RR-T5RL, RR-T10RL, RR-T15RL, etc.)
 *
 * @author Bob Adair - Initial contribution, partly based on Allan Tong's KeypadHandler class
 */
public class PicoKeypadHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        // Buttons for 2B, 2BRL, 3B, and 3BRL models
        BUTTON1(2, "button1"),
        BUTTON2(3, "button2"),
        BUTTON3(4, "button3"),
        RAISE(5, "buttonraise"),
        LOWER(6, "buttonlower"),

        // Buttons for PJ2-4B model
        BUTTON01(8, "button01"),
        BUTTON02(9, "button02"),
        BUTTON03(10, "button03"),
        BUTTON04(11, "button04");

        private final int id;
        private final String channel;

        COMPONENT(final int i, final String c) {
            id = i;
            channel = c;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public String channel() {
            return this.channel;
        }

        public static boolean isLed(int id) {
            return false; // No LEDs on Picos
        }

        public static boolean isButton(int id) {
            return (id >= 2 && id <= 11);
        }

    }

    private Logger logger = LoggerFactory.getLogger(PicoKeypadHandler.class);

    public PicoKeypadHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void configureComponents(String model) {
        model = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", model);

        switch (model) {
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
                this.logger.warn("No valid keypad model defined ({}). Assuming model 3BRL.", model);
            case "3BRL":
                buttonList = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3, COMPONENT.RAISE,
                        COMPONENT.LOWER);
                break;
        }
    }

}
