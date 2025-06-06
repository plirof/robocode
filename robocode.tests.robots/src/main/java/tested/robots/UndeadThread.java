/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package tested.robots;


import robocode.AdvancedRobot;


/**
 * @author Pavel Savara (original)
 */
public class UndeadThread extends AdvancedRobot {

	@Override
	public void run() {
		out.println("I will live forever!");
		// noinspection InfiniteLoopStatement
		while (true) {
			try {
				body();
			} catch (Throwable t) {
				// spamming the console
				out.println("Swalowed it, HA HA HA HA HAAAAA !!!!!");
				out.println(t);
			}
		}
	}

	private void body() {
		// noinspection InfiniteLoopStatement
		for (;;) {
			turnLeft(100);
			ahead(10);
			turnLeft(100);
			back(10);
		}
	}

}
