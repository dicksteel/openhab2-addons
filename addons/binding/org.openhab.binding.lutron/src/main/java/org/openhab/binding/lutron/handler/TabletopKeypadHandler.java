/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Handler responsible for communicating with Lutron Tabletop seeTouch keypads used in RadioRA2 and Homeworks QS systems
 * (e.g. RR-T5RL, RR-T10RL, RR-T15RL, etc.)
 *
 * @author Bob Adair - Initial contribution, partly based on Allan Tong's KeypadHandler class
 */
public class TabletopKeypadHandler extends LutronHandler {

    private static enum COMPONENT {
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

        public int id() {
            return this.id;
        }

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

    private static final Integer ACTION_PRESS = 3;
    private static final Integer ACTION_RELEASE = 4;
    private static final Integer ACTION_LED_STATE = 9;

    private static final Integer LED_OFF = 0;
    private static final Integer LED_ON = 1;

    private List<COMPONENT> buttonList = new ArrayList<COMPONENT>();
    private List<COMPONENT> ledList = new ArrayList<COMPONENT>();

    private int integrationId;

    private String model;

    private BiMap<Integer, String> ComponentChannelMap = HashBiMap.create(50);

    private Logger logger = LoggerFactory.getLogger(TabletopKeypadHandler.class);

    public TabletopKeypadHandler(Thing thing) {
        super(thing);
    }

    private void configureComponents(String model) {
        model = model == null ? "null" : model;
        this.logger.debug("Configuring components for keypad model {}", model);

        switch (model) {
            default:
                this.logger.info("No valid keypad model defined ({}). Assuming model T15RL.", model);
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

    private void configureChannels() {
        Channel channel;
        ChannelTypeUID channelTypeUID;
        List<Channel> channelList = new ArrayList<Channel>();
        ThingBuilder thingBuilder = editThing();

        logger.debug("Configuring channels for keypad");

        // add channels for buttons
        for (COMPONENT component : buttonList) {
            // channelTypeUID = new ChannelTypeUID(getThing().getUID().getAsString() + ":" + component.channel());
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "button");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "String")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        // add channels for LEDs
        for (COMPONENT component : ledList) {
            // channelTypeUID = new ChannelTypeUID(getThing().getUID().getAsString() + ":" + component.channel());
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "ledIndicator");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "Switch")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    private ChannelUID channelFromComponent(int component) {
        String channel = null;

        // Get channel string from Lutron component ID using HashBiMap
        channel = ComponentChannelMap.get(component);
        if (channel == null) {
            this.logger.debug("Unknown component {}", component);
        }
        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);
    }

    private Integer componentFromChannel(ChannelUID channelUID) {
        Integer id = ComponentChannelMap.inverse().get(channelUID.getId());
        return id;
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        this.logger.debug("Initializing Tabletop Keypad handler for integration ID {}", id);

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();

        this.model = (String) getThing().getConfiguration().get("model");

        configureComponents(this.model);

        // load the channel-id map
        for (COMPONENT component : buttonList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }
        for (COMPONENT component : ledList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }

        configureChannels();

        updateStatus(ThingStatus.ONLINE);

        // query the status of all keypad LEDs
        for (COMPONENT component : ledList) {
            queryDevice(component.id(), ACTION_LED_STATE);
        }
        return;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, Command command) {

        logger.debug("Command {}  for {}", command, channelUID);

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID,
                    getThing().getUID().toString());
            return;
        }

        Integer componentID = componentFromChannel(channelUID);
        if (componentID == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID,
                    getThing().getUID().toString());
            return;
        }

        if (COMPONENT.isLed(componentID)) {
            if (command instanceof RefreshType) {
                queryDevice(componentID, ACTION_LED_STATE);
                return;
            }

            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    device(componentID, ACTION_LED_STATE, LED_ON);
                } else if (command == OnOffType.OFF) {
                    device(componentID, ACTION_LED_STATE, LED_OFF);
                } else {
                    logger.warn("Assertion failure: OnOffType command state is neither ON nor OFF");
                }
            } else {
                logger.warn("Invalid command {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }

        if (COMPONENT.isButton(componentID)) {
            // TODO: Fix button channel sending
            // if (command instanceof StringType) {
            // device(componentID, ACTION_PRESS);
            // } else {
            logger.warn("Invalid command {} received for channel {} device {}", command, channelUID,
                    getThing().getUID());
            // }
            return;
        }

    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        this.logger.debug("Linking keypad channel {}", channelUID.getId());

        Integer id = componentFromChannel(channelUID);
        if (id == null) {
            this.logger.warn("Unrecognized channel ID {} linked", channelUID.getId());
            return;
        }

        // if this channel is for an LED, query the current state
        if (COMPONENT.isLed(id)) {
            queryDevice(id, ACTION_LED_STATE);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        this.logger.debug("Handling command {} {} from keypad", type.toString(), parameters);
        if (type == LutronCommandType.DEVICE && parameters.length >= 2) {
            int component;

            try {
                component = Integer.parseInt(parameters[0]);
            } catch (NumberFormatException e) {
                this.logger.error("Invalid component {} in keypad update event message", parameters[0]);
                return;
            }

            ChannelUID channelUID = channelFromComponent(component);

            if (channelUID != null) {
                if (ACTION_LED_STATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                    if (LED_ON.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.ON);
                    } else if (LED_OFF.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (ACTION_PRESS.toString().equals(parameters[1])) {
                    postCommand(channelUID, OnOffType.ON);
                } else if (ACTION_RELEASE.toString().equals(parameters[1])) {
                    postCommand(channelUID, OnOffType.OFF);
                }
            }
        }
    }

}
