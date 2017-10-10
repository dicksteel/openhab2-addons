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
import java.util.List;

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
public class TabletopKeypadHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        BUTTON1(1, "button1"),
        BUTTON2(2, "button2"),
        BUTTON3(3, "button3"),
        BUTTON4(4, "button4"),
        BUTTON5(5, "button5"),
        BUTTON6(6, "button6"),
        BUTTON7(7, "button7"),
        BUTTON8(8, "button8"),
        BUTTON9(9, "button9"),
        BUTTON10(10, "button10"),
        BUTTON11(11, "button11"),
        BUTTON12(12, "button12"),
        BUTTON13(13, "button13"),
        BUTTON14(14, "button14"),
        BUTTON15(15, "button15"),

        BUTTON16(16, "button16"),
        BUTTON17(17, "button17"),

        LOWER1(20, "buttonlower1"),
        RAISE1(21, "buttonraise1"),
        LOWER2(22, "buttonlower2"),
        RAISE2(23, "buttonraise2"),
        LOWER3(24, "buttonlower3"),
        RAISE3(25, "buttonraise3"),

        LED1(81, "led1"),
        LED2(82, "led2"),
        LED3(83, "led3"),
        LED4(84, "led4"),
        LED5(85, "led5"),
        LED6(86, "led6"),
        LED7(87, "led7"),
        LED8(88, "led8"),
        LED9(89, "led9"),
        LED10(90, "led10"),
        LED11(91, "led11"),
        LED12(92, "led12"),
        LED13(93, "led13"),
        LED14(94, "led14"),
        LED15(95, "led15");

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
            return (id >= 81 && id <= 95);
        }

        public static boolean isButton(int id) {
            return (id >= 1 && id <= 25);
        }

    }

    private static final List<COMPONENT> buttonGroup1 = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2,
            COMPONENT.BUTTON3, COMPONENT.BUTTON4, COMPONENT.BUTTON5);
    private static final List<COMPONENT> buttonGroup2 = Arrays.asList(COMPONENT.BUTTON6, COMPONENT.BUTTON7,
            COMPONENT.BUTTON8, COMPONENT.BUTTON9, COMPONENT.BUTTON10);
    private static final List<COMPONENT> buttonGroup3 = Arrays.asList(COMPONENT.BUTTON11, COMPONENT.BUTTON12,
            COMPONENT.BUTTON13, COMPONENT.BUTTON14, COMPONENT.BUTTON15);

    private static final List<COMPONENT> buttonsBottomRL = Arrays.asList(COMPONENT.BUTTON16, COMPONENT.BUTTON17,
            COMPONENT.LOWER3, COMPONENT.RAISE3);
    private static final List<COMPONENT> buttonsBottomCRL = Arrays.asList(COMPONENT.LOWER1, COMPONENT.RAISE1,
            COMPONENT.LOWER2, COMPONENT.RAISE2, COMPONENT.LOWER3, COMPONENT.RAISE3);

    private static final List<COMPONENT> ledGroup1 = Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3,
            COMPONENT.LED4, COMPONENT.LED5);
    private static final List<COMPONENT> ledGroup2 = Arrays.asList(COMPONENT.LED6, COMPONENT.LED7, COMPONENT.LED8,
            COMPONENT.LED9, COMPONENT.LED10);
    private static final List<COMPONENT> ledGroup3 = Arrays.asList(COMPONENT.LED11, COMPONENT.LED12, COMPONENT.LED13,
            COMPONENT.LED14, COMPONENT.LED15);

    private Logger logger = LoggerFactory.getLogger(TabletopKeypadHandler.class);

    @Override
    protected void configureComponents(String model) {
        model = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", model);

        switch (model) {
            default:
                this.logger.warn("No valid keypad model defined ({}). Assuming model T15RL.", model);
            case "T15RL":
                buttonList.addAll(buttonGroup3);
                ledList.addAll(ledGroup3);
            case "T10RL":
                buttonList.addAll(buttonGroup2);
                ledList.addAll(ledGroup2);
            case "T5RL":
                buttonList.addAll(buttonGroup1);
                buttonList.addAll(buttonsBottomRL);
                ledList.addAll(ledGroup1);
                break;

            case "T15CRL":
                buttonList.addAll(buttonGroup3);
                ledList.addAll(ledGroup3);
            case "T10CRL":
                buttonList.addAll(buttonGroup2);
                ledList.addAll(ledGroup2);
            case "T5CRL":
                buttonList.addAll(buttonGroup1);
                buttonList.addAll(buttonsBottomCRL);
                ledList.addAll(ledGroup1);
                break;
        }
    }

    public TabletopKeypadHandler(Thing thing) {
        super(thing);
    }

}
