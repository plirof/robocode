/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.test.robots;


import net.sf.robocode.io.Logger;
import net.sf.robocode.test.helpers.Assert;
import net.sf.robocode.test.helpers.RobocodeTestBed;
import org.junit.Test;

import javax.swing.*;


/**
 * @author Pavel Savara (original)
 */
public class TestAwtAttack extends RobocodeTestBed {
    // in case: boolean messagedAttack;
    boolean messagedBreakthru;

    @Test
    public void run() {
        super.run();
    }

	/* in case that we don't block JFrame by classloader
	 public void onTurnEnded(TurnEndedEvent event) {
	 super.onTurnEnded(event);
	 final String out = event.getTurnSnapshot().getRobots()[1].getOutputStreamSnapshot();

	 if (out.contains("Hacked!!!")) {
	 messagedBreakthru = true;
	 }
	 if (out.contains("Robots are not allowed to reference javax.swing package")) {
	 messagedAttack = true;
	 }
	 } */

    @Override
    public String getRobotName() {
        return "tested.robots.AwtAttack";
    }

    JFrame frame;

    @Override
    protected void runSetup() {
        if (java.awt.GraphicsEnvironment.isHeadless()) return;
        frame = new JFrame();
        frame.setVisible(true);
    }

    @Override
    protected int getExpectedErrors() {
        return 2;
    }

    @Override
    public int getExpectedRobotCount(String list) {
        return 1;
    }

    @Override
    protected void runTeardown() {
        if (java.awt.GraphicsEnvironment.isHeadless()) return;

        Runnable doCheck = new Runnable() {
            public void run() {
                Logger.logMessage("works still!!!");
            }
        };

        javax.swing.SwingUtilities.invokeLater(doCheck);

        frame.setVisible(false);
        Assert.assertFalse(messagedBreakthru);
        // in case: Assert.assertTrue(messagedAttack);
    }
}
