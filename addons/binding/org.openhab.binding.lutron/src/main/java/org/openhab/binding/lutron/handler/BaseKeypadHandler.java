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
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Abstract class providing common definitions and methods for derived keypad classes
 *
 * @author Bob Adair - Initial contribution, based partly on Allan Tong's KeypadHandler class
 */
public abstract class BaseKeypadHandler extends LutronHandler {

    protected static enum Component implements KeypadComponent {
        BUTTON1(1, "button1"),
        LED1(81, "led1");

        private int id;
        private String channel;

        Component(final int i, final String c) {
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

    protected static final Integer ACTION_PRESS = 3;
    protected static final Integer ACTION_RELEASE = 4;
    protected static final Integer ACTION_LED_STATE = 9;

    protected static final Integer LED_OFF = 0;
    protected static final Integer LED_ON = 1;

    protected List<KeypadComponent> buttonList = new ArrayList<KeypadComponent>();
    protected List<KeypadComponent> ledList = new ArrayList<KeypadComponent>();

    protected int integrationId;

    protected String model;

    protected BiMap<Integer, String> ComponentChannelMap = HashBiMap.create(50);

    private Logger logger = LoggerFactory.getLogger(BaseKeypadHandler.class);

    protected abstract void configureComponents(String model);

    public BaseKeypadHandler(Thing thing) {
        super(thing);
    }

    protected void configureChannels() {
        Channel channel;
        ChannelTypeUID channelTypeUID;
        List<Channel> channelList = new ArrayList<Channel>();
        ThingBuilder thingBuilder = editThing();

        logger.debug("Configuring channels for keypad");

        // add channels for buttons
        for (KeypadComponent component : buttonList) {
            // channelTypeUID = new ChannelTypeUID(getThing().getUID().getAsString() + ":" + component.channel());
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "buttonEvent");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "String")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        // add channels for LEDs
        for (KeypadComponent component : ledList) {
            // channelTypeUID = new ChannelTypeUID(getThing().getUID().getAsString() + ":" + component.channel());
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "ledIndicator");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "Switch")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    protected ChannelUID channelFromComponent(int component) {
        String channel = null;

        // Get channel string from Lutron component ID using HashBiMap
        channel = ComponentChannelMap.get(component);
        if (channel == null) {
            this.logger.debug("Unknown component {}", component);
        }
        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);
    }

    protected Integer componentFromChannel(ChannelUID channelUID) {
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
        this.logger.debug("Initializing Keypad Handler for integration ID {}", id);

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();

        this.model = ((String) getThing().getConfiguration().get("model")).toUpperCase();
        if (this.model.contains("-")) {
            // strip off system prefix if model is of the form "system-model"
            String[] modelSplit = this.model.split("-", 2);
            this.model = modelSplit[1];
        }

        configureComponents(this.model);

        // load the channel-id map
        for (KeypadComponent component : buttonList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }
        for (KeypadComponent component : ledList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }

        configureChannels();

        updateStatus(ThingStatus.ONLINE);

        // query the status of all keypad LEDs
        for (KeypadComponent component : ledList) {
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

        if (KeypadComponent.isLed(componentID)) {
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

        if (KeypadComponent.isButton(componentID)) {
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
        if (KeypadComponent.isLed(id)) {
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
                    // postCommand(channelUID, OnOffType.ON);
                    triggerChannel(channelUID, "BUTTON_PRESS");
                } else if (ACTION_RELEASE.toString().equals(parameters[1])) {
                    // postCommand(channelUID, OnOffType.OFF);
                    triggerChannel(channelUID, "BUTTON_RELEASE");
                }
            }
        }
    }

}
