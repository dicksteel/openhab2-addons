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
        // This "pseudo-abstract" static enum should be "overridden" in subclasses
        // by creating a new COMPONENT enum implementing the KeypadComponent interface.
        ;

        @Override
        public int id() {
            return 0;
        }

        @Override
        public String channel() {
            return null;
        }
    }

    protected static final Integer ACTION_PRESS = 3;
    protected static final Integer ACTION_RELEASE = 4;
    protected static final Integer ACTION_LED_STATE = 9;

    protected static final Integer LED_OFF = 0;
    protected static final Integer LED_ON = 1;
    protected static final Integer LED_FLASH = 2;
    protected static final Integer LED_RAPIDFLASH = 3;

    protected List<KeypadComponent> buttonList = new ArrayList<KeypadComponent>();
    protected List<KeypadComponent> ledList = new ArrayList<KeypadComponent>();
    protected List<KeypadComponent> cciList = new ArrayList<KeypadComponent>(); // for VCRX

    protected int integrationId;

    protected String model;

    protected Boolean autoRelease;

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
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "button");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "Switch")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        // add channels for LEDs
        for (KeypadComponent component : ledList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "ledIndicator");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "Switch")
                    .withType(channelTypeUID).build();
            channelList.add(channel);
        }

        // add channels for CCIs (for VCRX or eventually HomeWorks CCI)
        for (KeypadComponent component : cciList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "cciState");
            channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), component.channel()), "Contact")
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
        // TODO: Configure channels asynchronously because it is taking slightly over 5 seconds
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        this.logger.debug("Initializing Keypad Handler for integration ID {}", id);

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();

        this.model = (String) getThing().getConfiguration().get("model");
        if (this.model != null) {
            this.model.toUpperCase();
            if (this.model.contains("-")) {
                // strip off system prefix if model is of the form "system-model"
                String[] modelSplit = this.model.split("-", 2);
                this.model = modelSplit[1];
            }
        }

        Boolean ar = (Boolean) getThing().getConfiguration().get("autorelease");
        if (ar == null) {
            this.autoRelease = false;
        } else {
            this.autoRelease = ar;
        }

        configureComponents(this.model);

        // load the channel-id map
        for (KeypadComponent component : buttonList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }
        for (KeypadComponent component : ledList) {
            ComponentChannelMap.put(component.id(), component.channel());
        }
        for (KeypadComponent component : cciList) {
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

        logger.debug("Handling command {} for channel {}", command, channelUID);

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

        // For LEDs, handle RefreshType and OnOffType commands
        // TODO: Add support for flash & fastflash string commands for appropriate keypad models
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
                    assert false : "OnOffType command state is neither ON nor OFF";
                }
            } else {
                logger.warn("Invalid command {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }

        // For buttons and CCIs, handle OnOffType commands
        if (KeypadComponent.isButton(componentID) || KeypadComponent.isCCI(componentID)) {

            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    device(componentID, ACTION_PRESS);
                    if (this.autoRelease) {
                        device(componentID, ACTION_RELEASE);
                    }
                } else if (command == OnOffType.OFF) {
                    device(componentID, ACTION_RELEASE);
                }
            } else {
                logger.warn("Invalid command type {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        this.logger.debug("Linking keypad {} channel {}", integrationId, channelUID.getId());

        Integer id = componentFromChannel(channelUID);
        if (id == null) {
            this.logger.warn("Unrecognized channel ID {} linked", channelUID.getId());
            return;
        }

        // if this channel is for an LED, query the Lutron controller for the current state
        if (KeypadComponent.isLed(id)) {
            queryDevice(id, ACTION_LED_STATE);
        }
        // Button and CCI state can't be queried, only monitored for updates.
        // Init button state to OFF on channel init.
        if (KeypadComponent.isButton(id)) {
            updateState(channelUID, OnOffType.OFF);
        }
        // Leave CCI channel state undefined on channel init.
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        this.logger.debug("Handling command {} {} from keypad {}", type.toString(), parameters, integrationId);
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
                    // TODO: Handle LED_FLASH and LED_RAPIDFLASH states
                } else if (ACTION_PRESS.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.ON);
                } else if (ACTION_RELEASE.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.OFF);
                }
            } else {
                this.logger.warn("Unable to determine channel for component {} in keypad update event message",
                        parameters[0]);
            }
        }
    }

}
