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
 * Handler responsible for communicating with Lutron seeTouch and Hybrid seeTouch keypads used in
 * RadioRA2 and Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
public class KeypadHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        BUTTON1(1, "button1"),
        BUTTON2(2, "button2"),
        BUTTON3(3, "button3"),
        BUTTON4(4, "button4"),
        BUTTON5(5, "button5"),
        BUTTON6(6, "button6"),
        BUTTON7(7, "button7"),

        LOWER1(16, "buttontoplower"),
        RAISE1(17, "buttontopraise"),
        LOWER2(18, "buttonbottomlower"),
        RAISE2(19, "buttonbottomraise"),

        // CCI1(25, "cci1"), // listed in spec but currently unused in binding
        // CCI2(26, "cci2"), // listed in spec but currently unused in binding

        LED1(81, "led1"),
        LED2(82, "led2"),
        LED3(83, "led3"),
        LED4(84, "led4"),
        LED5(85, "led5"),
        LED6(86, "led6"),
        LED7(87, "led7");

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

    }

    private Logger logger = LoggerFactory.getLogger(KeypadHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 87);
    }

    @Override
    protected boolean isButton(int id) {
        return ((id >= 1 && id <= 7) || (id >= 16 && id <= 19));
    }

    @Override
    protected boolean isCCI(int id) {
        // return (id >= 25 && id <= 26);
        return false;
    }

    @Override
    protected void configureComponents(String model) {
        String mod = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            case "W1RLD":
            case "H1RLD":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON5, COMPONENT.BUTTON6));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(
                        Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED5, COMPONENT.LED6));
                break;
            case "W2RLD":
            case "H2RLD":
                buttonList.addAll(
                        Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON5, COMPONENT.BUTTON6));
                buttonList
                        .addAll(Arrays.asList(COMPONENT.LOWER1, COMPONENT.RAISE1, COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED5, COMPONENT.LED6));
                break;
            case "W3S":
            case "H3S":
                buttonList.addAll(
                        Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3, COMPONENT.BUTTON6));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED6));
                break;
            case "W3BD":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON5, COMPONENT.BUTTON6, COMPONENT.BUTTON7));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED5,
                        COMPONENT.LED6, COMPONENT.LED7));
                break;
            case "W3BRL":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON2, COMPONENT.BUTTON3, COMPONENT.BUTTON4));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4));
                break;
            case "W3BSRL":
            case "H3BSRL":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON3, COMPONENT.BUTTON5));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED3, COMPONENT.LED5));
                break;
            case "W4S":
            case "H4S":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON4, COMPONENT.BUTTON6));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(
                        Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4, COMPONENT.LED6));
                break;
            case "W5BRL":
            case "H5BRL":
            case "W5BRLIR":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON4, COMPONENT.BUTTON5));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(
                        Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4, COMPONENT.LED5));
                break;
            case "W6BRL":
            case "H6BRL":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON4, COMPONENT.BUTTON5, COMPONENT.BUTTON6));
                buttonList.addAll(Arrays.asList(COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4,
                        COMPONENT.LED5, COMPONENT.LED6));
                break;
            case "W7B":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON4, COMPONENT.BUTTON5, COMPONENT.BUTTON6, COMPONENT.BUTTON7));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4,
                        COMPONENT.LED5, COMPONENT.LED6, COMPONENT.LED7));
                break;
            default:
                this.logger.warn("No valid keypad model defined ({}). Assuming Generic model.", mod);
            case "Generic":
                buttonList.addAll(Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2, COMPONENT.BUTTON3,
                        COMPONENT.BUTTON4, COMPONENT.BUTTON5, COMPONENT.BUTTON6, COMPONENT.BUTTON7));
                buttonList
                        .addAll(Arrays.asList(COMPONENT.LOWER1, COMPONENT.RAISE1, COMPONENT.LOWER2, COMPONENT.RAISE2));
                ledList.addAll(Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3, COMPONENT.LED4,
                        COMPONENT.LED5, COMPONENT.LED6, COMPONENT.LED7));
                break;

        }
    }

    public KeypadHandler(Thing thing) {
        super(thing);
    }

}
