/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Pavel Savara & Flemming N. Larsen
 *     - Initial implementation
 *******************************************************************************/
package robocode.recording;


import robocode.battle.BaseBattle;
import robocode.battle.events.*;
import robocode.battle.snapshot.TurnSnapshot;
import static robocode.io.Logger.logError;
import robocode.manager.RobocodeManager;

import java.io.*;
import java.util.zip.ZipInputStream;


/**
 * @author Pavel Savara (original)
 * @author Flemming N. Larsen (original)
 */
public final class BattlePlayer extends BaseBattle {

	private RecordManager recordManager;

	public BattlePlayer(RobocodeManager manager, RecordManager recordManager, BattleEventDispatcher eventDispatcher) {
		super(manager, eventDispatcher, false);
		this.recordManager = recordManager;
	}

	@Override
	protected void initializeBattle() {
		super.initializeBattle();

		battleRules = recordManager.recordInfo.battleRules;

		eventDispatcher.onBattleStarted(
				new BattleStartedEvent(recordManager.readSnapshot(currentTime), battleRules, true));
		if (isPaused()) {
			eventDispatcher.onBattlePaused(new BattlePausedEvent());
		}
	}

	@Override
	protected void finalizeBattle() {
		boolean aborted = recordManager.recordInfo.results == null || isAborted();

		eventDispatcher.onBattleEnded(new BattleEndedEvent(aborted));

		if (!aborted) {
			eventDispatcher.onBattleCompleted(new BattleCompletedEvent(battleRules, recordManager.recordInfo.results));
		}

		super.finalizeBattle();

		cleanup();
	}

	@Override
	protected void initializeRound() {
		super.initializeRound();

		eventDispatcher.onRoundStarted(new RoundStartedEvent(getRoundNum()));
	}

	@Override
	protected void finalizeRound() {
		super.finalizeRound();

		eventDispatcher.onRoundEnded(new RoundEndedEvent(getRoundNum(), getTime()));
	}

	@Override
	protected void initializeTurn() {
		super.initializeTurn();

		eventDispatcher.onTurnStarted(new TurnStartedEvent());
	}

	@Override
	protected void finalizeTurn() {
		eventDispatcher.onTurnEnded(new TurnEndedEvent(recordManager.readSnapshot(currentTime)));

		super.finalizeTurn();
	}

	@Override
	protected boolean isRoundOver() {
		return (isAborted() || getTime() > recordManager.recordInfo.turnsInRounds[getRoundNum()]);
	}

}