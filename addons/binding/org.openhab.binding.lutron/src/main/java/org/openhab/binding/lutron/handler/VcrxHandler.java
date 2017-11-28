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
 * Handler responsible for communicating with Lutron VCRX visor control receiver
 *
 * @author Bob Adair - Initial contribution
 */
public class VcrxHandler extends BaseKeypadHandler {

    private static enum COMPONENT implements KeypadComponent {
        BUTTON1(1, "button1"),
        BUTTON2(2, "button2"),
        BUTTON3(3, "button3"),
        BUTTON4(4, "button4"),
        BUTTON5(5, "button5"),
        BUTTON6(6, "button6"),

        CCI1(30, "cci1"),
        CCI2(31, "cci2"),
        CCI3(32, "cci3"),
        CCI4(33, "cci4"),

        LED1(81, "led1"),
        LED2(82, "led2"),
        LED3(83, "led3"),
        LED4(84, "led4"),
        LED5(85, "led5"),
        LED6(86, "led6");

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

    private static final List<COMPONENT> buttonGroup = Arrays.asList(COMPONENT.BUTTON1, COMPONENT.BUTTON2,
            COMPONENT.BUTTON3, COMPONENT.BUTTON4, COMPONENT.BUTTON5, COMPONENT.BUTTON6);

    private static final List<COMPONENT> ledGroup = Arrays.asList(COMPONENT.LED1, COMPONENT.LED2, COMPONENT.LED3,
            COMPONENT.LED4, COMPONENT.LED5, COMPONENT.LED6);

    private static final List<COMPONENT> cciGroup = Arrays.asList(COMPONENT.CCI1, COMPONENT.CCI2, COMPONENT.CCI3,
            COMPONENT.CCI4);

    private Logger logger = LoggerFactory.getLogger(VcrxHandler.class);

    @Override
    protected boolean isLed(int id) {
        return (id >= 81 && id <= 86);
    }

    @Override
    protected boolean isButton(int id) {
        return (id >= 1 && id <= 6);
    }

    @Override
    protected boolean isCCI(int id) {
        return (id >= 30 && id <= 33);
    }

    @Override
    protected void configureComponents(String model) {
        this.logger.debug("Configuring components for VCRX");

        buttonList.addAll(buttonGroup);
        ledList.addAll(ledGroup);
        cciList.addAll(cciGroup);
    }

    public VcrxHandler(Thing thing) {
        super(thing);
    }

}
