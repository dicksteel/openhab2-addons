/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_TRIGGER;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron pulsed CCO outputs.
 * e.g. VCRX outputs and CCO module outputs
 *
 * @author Bob Adair - Initial contribution
 */
public class CcoHandler extends LutronHandler {
    private static final Integer ACTION_PULSE = 6;

    private int integrationId;
    private double defaultPulse = 0.5; // default pulse length (seconds)

    private Logger logger = LoggerFactory.getLogger(CcoHandler.class);

    public CcoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        Number defaultPulse = (Number) getThing().getConfiguration().get("defaultPulse");

        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();

        if (defaultPulse != null) {
            double dp = defaultPulse.doubleValue();
            if (dp >= 0 && dp <= 100.0) {
                this.defaultPulse = dp;
                logger.info("Default pulse length set to {} seconds", defaultPulse.toString());
            } else {
                logger.warn("Invalid default pulse length value set. Defaulting to 0.5s.");
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: Fix for proper channel types & command handling
        // Should support On/Off and Number command types (pulse length)
        if (command instanceof OnOffType && command == OnOffType.ON) {
            // TODO: Get pulse length from defaultPulse or channel
            output(ACTION_PULSE, 0.5);
            updateState(channelUID, OnOffType.OFF);
        }
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        logger.debug("Update received for CCO: {} {}", type, StringUtils.join(parameters, ","));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_TRIGGER)) {
            logger.debug("trigger channel {} linked for CCO", channelUID.getId().toString());
        } else {
            // TODO: Can/should we throw an exception here?
            logger.warn("invalid channel {} linked for CCO", channelUID.getId().toString());
        }
    }
}
