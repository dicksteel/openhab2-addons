/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.*;

import java.math.BigDecimal;

//import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the RA2/HWQS time clock.
 *
 * @author Bob Adair - Initial contribution
 */
public class TimeclockHandler extends LutronHandler {
    private static final Integer ACTION_CLOCKMODE = 1;
    private static final Integer ACTION_SUNRISE = 2;
    private static final Integer ACTION_SUNSET = 3;
    private static final Integer ACTION_SCHEDULE = 4;
    private static final Integer ACTION_EXECEVENT = 5;
    private static final Integer ACTION_ENABLEEVENT = 6;

    private Logger logger = LoggerFactory.getLogger(TimeclockHandler.class);

    // private TimeclockConfig config;
    private int integrationId;

    public TimeclockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        this.logger.debug("Initializing timeclock handler");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        this.integrationId = id.intValue();
        updateStatus(ThingStatus.ONLINE);

        // TODO: Is this initialization really needed here?
        queryTimeclock(ACTION_CLOCKMODE);
        queryTimeclock(ACTION_SUNRISE);
        queryTimeclock(ACTION_SUNSET);
        // queryTimeclock(ACTION_SCHEDULE);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        this.logger.debug("Handling TIMECLOCK channel link request");
        switch (channelUID.getId()) {
            case CHANNEL_CLOCKMODE:
                queryTimeclock(ACTION_CLOCKMODE);
                break;
            case CHANNEL_SUNRISE:
                queryTimeclock(ACTION_SUNRISE);
                break;
            case CHANNEL_SUNSET:
                queryTimeclock(ACTION_SUNSET);
                break;
            // case CHANNEL_SCHEDULE:
            // queryTimeclock(ACTION_SCHEDULE);
            // break;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.debug("Handling timeclock command");
        if (channelUID.getId().equals(CHANNEL_CLOCKMODE) && (command instanceof Number)) {
            BigDecimal mode = new BigDecimal(((Number) command).intValue());
            timeclock(ACTION_CLOCKMODE, mode);
        } else if (channelUID.getId().equals(CHANNEL_EXECEVENT) && (command instanceof Number)) {
            BigDecimal index = new BigDecimal(((Number) command).intValue());
            timeclock(ACTION_CLOCKMODE, index);
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type != LutronCommandType.TIMECLOCK) {
            return;
        }
        this.logger.debug("Handling reveived timeclock update");
        if (parameters.length > 1 && ACTION_CLOCKMODE.toString().equals(parameters[0])) {
            BigDecimal mode = new BigDecimal(parameters[1]);
            updateState(CHANNEL_CLOCKMODE, new DecimalType(mode));
        } else if (parameters.length > 1 && ACTION_SUNRISE.toString().equals(parameters[0])) {
            updateState(CHANNEL_SUNRISE, new StringType(parameters[1]));
        } else if (parameters.length > 1 && ACTION_SUNSET.toString().equals(parameters[0])) {
            updateState(CHANNEL_SUNSET, new StringType(parameters[1]));
        } else if (parameters.length > 1 && ACTION_EXECEVENT.toString().equals(parameters[0])) {
            BigDecimal index = new BigDecimal(parameters[1]);
            updateState(CHANNEL_EXECEVENT, new DecimalType(index));
        }
    }
}
