/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.test.robots;


import net.sf.robocode.test.helpers.RobocodeTestBed;
import org.junit.Assert;
import org.junit.Test;
import robocode.control.events.TurnEndedEvent;


/**
 * @author Flemming N. Larsen (original)
 */
public class TestConstructorReflectionAttack extends RobocodeTestBed {

	private boolean messagedAccessDenied;

	@Test
	public void run() {
		super.run();
	}

	@Override
	public String getRobotName() {
		return "tested.robots.ConstructorReflectionAttack";
	}

	@Override
	public void onTurnEnded(TurnEndedEvent event) {
		super.onTurnEnded(event);

		final String out = event.getTurnSnapshot().getRobots()[0].getOutputStreamSnapshot();

		if (out.contains("access denied (java.lang.reflect.ReflectPermission")
				|| out.contains("access denied (\"java.lang.reflect.ReflectPermission\"")) {
			messagedAccessDenied = true;
		}
	}

	@Override
	protected void runTeardown() {
		Assert.assertTrue("Reflection is not allowed", messagedAccessDenied);
	}

	@Override
	protected int getExpectedErrors() {
		return 1; // Security error must be reported as an error
	}
}
