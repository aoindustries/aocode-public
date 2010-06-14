/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public.
 *
 * aocode-public is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.cron;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Each <code>CronJob</code> is ran in a separate <code>CronDaemonThread</code>.
 *
 * @author  AO Industries, Inc.
 */
final public class CronDaemonThread extends Thread {
    
    final CronJob cronJob;
    final Logger logger;
    final int minute;
    final int hour;
    final int dayOfMonth;
    final int month;
    final int dayOfWeek;
    final int year;
    
    CronDaemonThread(CronJob cronJob, Logger logger, int minute, int hour, int dayOfMonth, int month, int dayOfWeek, int year) {
        super(cronJob.getCronJobName());
        this.cronJob=cronJob;
        this.logger=logger;
        this.minute=minute;
        this.hour=hour;
        this.dayOfMonth=dayOfMonth;
        this.month=month;
        this.dayOfWeek=dayOfWeek;
        this.year = year;
    }
    
    /**
     * For internal API use only.
     */
    @Override
    public void run() {
        try {
            try {
                cronJob.runCronJob(minute, hour, dayOfMonth, month, dayOfWeek, year);
            } catch(ThreadDeath TD) {
                throw TD;
            } catch(Throwable T) {
                logger.log(Level.SEVERE, "cron_job.name="+cronJob.getCronJobName(), T);
            }
        } finally {
            CronDaemon.threadDone(this);
        }
    }
}
