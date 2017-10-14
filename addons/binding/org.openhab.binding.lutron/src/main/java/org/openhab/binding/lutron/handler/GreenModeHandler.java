/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_STEP;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with RA2 Green Mode subsystem
 *
 * @author Bob Adair - Initial contribution
 */
public class GreenModeHandler extends LutronHandler {
    private static final Integer ACTION_STEP = 1;

    private Logger logger = LoggerFactory.getLogger(GreenModeHandler.class);

    private int integrationId;

    public GreenModeHandler(Thing thing) {
        super(thing);
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
        updateStatus(ThingStatus.ONLINE);
        // TODO: add job to periodically query green mode step
        queryGreenMode(ACTION_STEP);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_STEP)) {
            queryGreenMode(ACTION_STEP);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_STEP)) {
            if (command.equals(OnOffType.ON)) {
                greenMode(ACTION_STEP, 2);
            } else if (command.equals(OnOffType.OFF)) {
                greenMode(ACTION_STEP, 1);
            } else if (command instanceof Number) {
                BigDecimal step = new BigDecimal(((Number) command).intValue());
                greenMode(ACTION_STEP, step);
            } else if (command instanceof RefreshType) {
                queryGreenMode(ACTION_STEP);
            } else {
                this.logger.info("Ignoring invalid command {}", command.toString());
            }
        } else {
            this.logger.info("Ignoring command to invalid channel {}", channelUID.getId());
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.MODE && parameters.length > 1 && ACTION_STEP.toString().equals(parameters[0])) {
            BigDecimal step = new BigDecimal(parameters[1]);
            // postCommand(CHANNEL_STEP, step);
            updateState(CHANNEL_STEP, new DecimalType(step));
        } else {
            this.logger.debug("Ignoring unexpected update");
        }
    }

}
