/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_SWITCH;

import java.util.Locale;

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
 * Handler responsible for communicating with Lutron pulsed cold contact outputs (CCOs).
 * e.g. VCRX CCO outputs and CCO RF module outputs
 *
 * @author Bob Adair - Initial contribution
 */

// Note: For a RA2 Pulsed CCO, querying the output state with ?OUTPUT,<id>,1 is meaningless and will always
// return 100 (on). Also, the main repeater will not report ~OUTPUT commands for a pulsed CCO regardless of
// the #MONITORING setting. So this binding supports sending pulses ONLY.

public class CcoHandler extends LutronHandler {
    private static final Integer ACTION_PULSE = 6;

    private int integrationId;
    private double defaultPulse = 0.5; // default pulse length (seconds)

    private Logger logger = LoggerFactory.getLogger(CcoHandler.class);

    public CcoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
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
                logger.info("Pulse length set to {} seconds for device {}.", defaultPulse.toString(), integrationId);
            } else {
                logger.warn("Invalid pulse length value set. Using default for device {}.", integrationId);
            }
        } else {
            logger.debug("Using default pulse length value for device {}", integrationId);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            logger.debug("switch channel {} linked for CCO {}", channelUID.getId().toString(), integrationId);
            // Since this is a pulsed CCO channel state is always OFF
            updateState(channelUID, OnOffType.OFF);
        } else {
            logger.warn("invalid channel {} linked for CCO {}", channelUID.getId().toString(), integrationId);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType && command == OnOffType.ON) {
            output(ACTION_PULSE, String.format(Locale.ROOT, "%.2f", defaultPulse));
            updateState(channelUID, OnOffType.OFF);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        // Do nothing on update for pulsed CCO. Repeater will only echo back our own output commands.
        logger.debug("Update received for CCO: {} {}", type, StringUtils.join(parameters, ","));
    }

}
