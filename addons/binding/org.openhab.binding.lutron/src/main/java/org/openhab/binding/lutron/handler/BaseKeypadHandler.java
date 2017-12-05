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
import java.util.concurrent.TimeUnit;

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
        // This silliness is necessary because Java does not support true overriding of enums in subclasses.
        ;

        @Override
        public int id() {
            return 0;
        }

        @Override
        public String channel() {
            return null;
        }

        @Override
        public String description() {
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
    protected Boolean advancedChannels = false;

    protected BiMap<Integer, String> ComponentChannelMap = HashBiMap.create(50);

    private Logger logger = LoggerFactory.getLogger(BaseKeypadHandler.class);

    protected abstract void configureComponents(String model);

    protected abstract boolean isLed(int id);

    protected abstract boolean isButton(int id);

    protected abstract boolean isCCI(int id);

    public BaseKeypadHandler(Thing thing) {
        super(thing);
    }

    private boolean channelExists(List<Channel> channels, ChannelUID channelUID) {
        if (channels == null) {
            return false;
        }
        for (Channel ch : channels) {
            if (ch.getUID().equals(channelUID)) {
                return true;
            }
        }
        return false;
    }

    protected void configureChannels() {
        Channel channel;
        ChannelTypeUID channelTypeUID;
        ChannelUID channelUID;
        List<Channel> channelList = new ArrayList<Channel>();
        ThingBuilder thingBuilder = editThing();

        logger.debug("Configuring channels for keypad {}", integrationId);

        List<Channel> oldChannels = getThing().getChannels();

        // add channels for buttons
        for (KeypadComponent component : buttonList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "buttonAdvanced" : "button");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            if (!channelExists(oldChannels, channelUID)) {
                channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                        .withLabel(component.description()).build();
                channelList.add(channel);
            }
        }

        // add channels for LEDs
        for (KeypadComponent component : ledList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "ledIndicatorAdvanced" : "ledIndicator");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            if (!channelExists(oldChannels, channelUID)) {
                channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                        .withLabel(component.description()).build();
                channelList.add(channel);
            }
        }

        // add channels for CCIs (for VCRX or eventually HomeWorks CCI)
        for (KeypadComponent component : cciList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "cciState");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            if (!channelExists(oldChannels, channelUID)) {
                channel = ChannelBuilder.create(channelUID, "Contact").withType(channelTypeUID)
                        .withLabel(component.description()).build();
                channelList.add(channel);
            }
        }

        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
        logger.debug("Done configuring channels for keypad {}", integrationId);
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
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();

        this.logger.debug("Initializing Keypad Handler for integration ID {}", id);

        this.model = (String) getThing().getConfiguration().get("model");
        if (this.model != null) {
            this.model.toUpperCase();
            if (this.model.contains("-")) {
                // strip off system prefix if model is of the form "system-model"
                String[] modelSplit = this.model.split("-", 2);
                this.model = modelSplit[1];
            }
        }

        Boolean arParam = (Boolean) getThing().getConfiguration().get("autorelease");
        if (arParam == null) {
            this.autoRelease = true;
        } else {
            this.autoRelease = arParam;
        }

        // now schedule a thread to finish initialization asynchronously
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                asyncInitialize();
            }
        }, 0, TimeUnit.SECONDS);
    }

    private synchronized void asyncInitialize() {
        this.logger.debug("Async init thread staring for keypad handler {}", integrationId);
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
        // for (KeypadComponent component : ledList) {
        // queryDevice(component.id(), ACTION_LED_STATE);
        // }

        this.logger.debug("Async init thread finishing for keypad handler {}", integrationId);
        return;
    }

    @Override
    public void dispose() {
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
        // TODO: Add support for FLASH & RAPIDFLASH string commands for appropriate keypad models
        if (isLed(componentID)) {
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
        if (isButton(componentID) || isCCI(componentID)) {

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
        if (isLed(id)) {
            queryDevice(id, ACTION_LED_STATE);
        }
        // Button and CCI state can't be queried, only monitored for updates.
        // Init button state to OFF on channel init.
        if (isButton(id)) {
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
                    if (this.autoRelease) {
                        updateState(channelUID, OnOffType.OFF);
                    }
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
