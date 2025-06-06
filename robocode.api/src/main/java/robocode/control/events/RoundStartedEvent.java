/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package robocode.control.events;


import robocode.control.snapshot.ITurnSnapshot;
import robocode.robotinterfaces.IBasicRobot;

import java.util.List;


/**
 * A RoundStartedEvent is sent to {@link IBattleListener#onRoundStarted(RoundStartedEvent)
 * onRoundStarted()} when a new round in a battle is started. 
 *
 * @see IBattleListener
 * @see RoundEndedEvent
 *
 * @author Pavel Savara (original)
 * @author Flemming N. Larsen (contributor)
 *
 * @since 1.6.2
 */
public class RoundStartedEvent extends BattleEvent {
	private final ITurnSnapshot startSnapshot;
	private final int round;
	private final List<IBasicRobot> robotObjects;

	/**
	 * Called by the game to create a new RoundStartedEvent.
	 * Please don't use this constructor as it might change.
	 *
	 * @param startSnapshot the start snapshot of the participating robots, initial starting positions etc.
	 * @param round the round number (zero indexed).
	 * @param robotObjects instances of robots for integration testing
	 */
	public RoundStartedEvent(ITurnSnapshot startSnapshot, int round, List<IBasicRobot> robotObjects) {
		super();
		this.startSnapshot = startSnapshot;
		this.round = round;
		this.robotObjects = robotObjects;
	}

	/**
	 * Returns the start snapshot of the participating robots, initial starting positions etc.
	 *
	 * @return a {@link robocode.control.snapshot.ITurnSnapshot} that serves as the start snapshot of
	 *         the round.
	 */
	public ITurnSnapshot getStartSnapshot() {
		return startSnapshot;
	}

	/**
	 * Returns the round number.
	 *
	 * @return the round number, which is zero indexed.
	 */
	public int getRound() {
		return round;
	}

	/**
	 * @return instances of robots for integration testing
	 */
	public List<IBasicRobot> getRobotObjects(){
		return robotObjects;
	}
}
