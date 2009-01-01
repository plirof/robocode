/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Code cleanup & optimizations
 *     - Removed getBattleView().setDoubleBuffered(false) as BufferStrategy is
 *       used now
 *     - Replaced FileSpecificationVector, RobotPeerVector, and
 *       RobotClassManagerVector with plain Vector
 *     - Added check for if GUI is enabled before using graphical components
 *     - Added restart() method
 *     - Ported to Java 5
 *     - Added support for the replay feature
 *     - Removed the clearBattleProperties()
 *     - Updated to use methods from FileUtil and Logger, which replaces methods
 *       that have been (re)moved from the robocode.util.Utils class
 *     - Added PauseResumeListener interface, addListener(), removeListener(),
 *       notifyBattlePaused(), notifyBattleResumed() for letting listeners
 *       receive notifications when the game is paused or resumed
 *     - Added missing functionality in to support team battles in
 *       startNewBattle(BattleSpecification spec, boolean replay)
 *     - Added missing close() on FileInputStreams and FileOutputStreams
 *     - isPaused() is now synchronized
 *     - Extended sendResultsToListener() to handle teams as well as robots
 *     - Added setDefaultBattleProperties() for resetting battle properties
 *     - Removed the showResultsDialog parameter from the stop() method
 *     - Added null pointer check to the sendResultsToListener() method
 *     - Enhanced the getBattleFilename() to look into the battle dir and also
 *       add the .battle file extension to the returned file name if this is
 *       missing
 *     - Removed battleRunning field, isBattleRunning(), and setBattle()
 *     - Bugfix: Multiple battle threads could run in the same time when the
 *       battle thread was started in startNewBattle()
 *     Luis Crespo
 *     - Added debug step feature, including the nextTurn(), shouldStep(),
 *       startNewRound()
 *     Robert D. Maupin
 *     - Replaced old collection types like Vector and Hashtable with
 *       synchronized List and HashMap
 *     Nathaniel Troutman
 *     - Bugfix: Added cleanup() to prevent memory leaks by removing circular
 *       references
 *     Pavel Savara
 *     - now driven by BattleObserver and commands to battle
 *     - initial code of battle recorder and player 
 *******************************************************************************/
package net.sf.robocode.battle;


import net.sf.robocode.IRobocodeManager;
import net.sf.robocode.battle.events.BattleEventDispatcher;
import net.sf.robocode.core.Container;
import net.sf.robocode.host.ICpuManager;
import net.sf.robocode.host.IHostManager;
import net.sf.robocode.io.FileUtil;
import net.sf.robocode.io.Logger;
import static net.sf.robocode.io.Logger.logError;
import static net.sf.robocode.io.Logger.logMessage;
import net.sf.robocode.recording.BattlePlayer;
import net.sf.robocode.recording.IRecordManager;
import net.sf.robocode.repository.IRepositoryManager;
import org.picocontainer.Characteristics;
import robocode.Event;
import robocode.control.BattleSpecification;
import robocode.control.RandomFactory;
import robocode.control.RobotSpecification;
import robocode.control.events.BattleFinishedEvent;
import robocode.control.events.BattlePausedEvent;
import robocode.control.events.BattleResumedEvent;
import robocode.control.events.IBattleListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Luis Crespo (contributor)
 * @author Robert D. Maupin (contributor)
 * @author Nathaniel Troutman (contributor)
 * @author Pavel Savara (contributor)
 */
public class BattleManager implements IBattleManager {
	private final IRobocodeManager manager;
	private final IHostManager hostManager;
	private final ICpuManager cpuManager;
	private final IRecordManager recordManager;
	private final IRepositoryManager repositoryManager;

	private volatile IBattle battle;
	private BattleProperties battleProperties = new BattleProperties();

	private final BattleEventDispatcher battleEventDispatcher;

	private String battleFilename;
	private String battlePath;

	private int pauseCount = 0;
	private final AtomicBoolean isManagedTPS = new AtomicBoolean(false);

	public BattleManager(IRobocodeManager manager, IRepositoryManager repositoryManager, IHostManager hostManager, ICpuManager cpuManager, BattleEventDispatcher battleEventDispatcher, IRecordManager recordManager) {
		this.manager = manager;
		this.recordManager = recordManager;
		this.repositoryManager = repositoryManager;
		this.cpuManager = cpuManager;
		this.hostManager = hostManager;
		this.battleEventDispatcher = battleEventDispatcher;
	}

	public synchronized void cleanup() {
		if (battle != null) {
			battle.waitTillOver();
			battle.cleanup();
			battle = null;
		}
	}

	// Called when starting a new battle from GUI
	public void startNewBattle(BattleProperties battleProperties, boolean waitTillOver) {
		this.battleProperties = battleProperties;

		List<RobotSpecification> battlingRobotsList = new ArrayList<RobotSpecification>();

		if (battleProperties.getSelectedRobots() != null) {
			StringTokenizer tokenizer = new StringTokenizer(battleProperties.getSelectedRobots(), ",");

			int num = 0;

			while (tokenizer.hasMoreTokens()) {
				String bot = tokenizer.nextToken();

				boolean failed = loadRobot(battlingRobotsList, bot, null, num);

				if (failed) {
					return;
				}
				num++;
			}
		}

		startNewBattleImpl(battlingRobotsList, waitTillOver);
	}

	// Called from the RobocodeEngine
	public void startNewBattle(BattleSpecification spec, boolean waitTillOver) {
		battleProperties = new BattleProperties();
		battleProperties.setBattlefieldWidth(spec.getBattlefield().getWidth());
		battleProperties.setBattlefieldHeight(spec.getBattlefield().getHeight());
		battleProperties.setGunCoolingRate(spec.getGunCoolingRate());
		battleProperties.setInactivityTime(spec.getInactivityTime());
		battleProperties.setNumRounds(spec.getNumRounds());
		battleProperties.setSelectedRobots(spec.getRobots());

		List<RobotSpecification> battlingRobotsList = new ArrayList<RobotSpecification>();

		int num = 0;

		for (robocode.control.RobotSpecification battleRobotSpec : spec.getRobots()) {
			if (battleRobotSpec == null) {
				break;
			}

			String bot = battleRobotSpec.getNameAndVersion();
			boolean failed = loadRobot(battlingRobotsList, bot, battleRobotSpec, num);

			num++;

			if (failed) {
				return;
			}
		}
		startNewBattleImpl(battlingRobotsList, waitTillOver);
	}

	private boolean loadRobot(List<RobotSpecification> battlingRobotsList, String bot, RobotSpecification battleRobotSpec, int teamNum) {
		boolean found = repositoryManager.load(battlingRobotsList, bot, battleRobotSpec, teamNum);

		if (!found) {
			logError("Aborting battle, could not find robot: " + bot);
			this.battleEventDispatcher.onBattleFinished(new BattleFinishedEvent(true));
			return true;
		}
		return false;
	}

	private void startNewBattleImpl(List<RobotSpecification> battlingRobotsList, boolean waitTillOver) {

		if (battle != null && battle.isRunning()) {
			battle.stop(true);
		}

		Logger.setLogListener(battleEventDispatcher);
		logMessage("Preparing battle...");

		final boolean recording = manager.getProperties().getOptionsCommonEnableReplayRecording()
				&& System.getProperty("TESTING", "none").equals("none");

		if (recording) {
			recordManager.attachRecorder(battleEventDispatcher);
		} else {
			recordManager.detachRecorder();
		}

		// resets seed for deterministic behavior of Random
		final String seed = System.getProperty("RANDOMSEED", "none");

		if (!seed.equals("none")) {
			// init soon as it reads random
			cpuManager.getCpuConstant();

			RandomFactory.resetDeterministic(Long.valueOf(seed));
		}

		Battle realBattle = Container.factory.as(Characteristics.NO_CACHE).getComponent(Battle.class);

		realBattle.setup(battlingRobotsList, battleProperties, isPaused());

		if (recording) {
			realBattle.setAllPaintRecorded(true);
		}

		battle = realBattle;

		Thread battleThread = new Thread(Thread.currentThread().getThreadGroup(), realBattle);

		battleThread.setPriority(Thread.NORM_PRIORITY);
		battleThread.setName("Battle Thread");
		realBattle.setBattleThread(battleThread);

		if (!System.getProperty("NOSECURITY", "false").equals("true")) {
			hostManager.addSafeThread(battleThread);
		}

		// Start the realBattle thread
		battleThread.start();

		// Wait until the realBattle is running and ended.
		// This must be done as a new realBattle could be started immediately after this one causing
		// multiple realBattle threads to run at the same time, which must be prevented!
		realBattle.waitTillStarted();
		if (waitTillOver) {
			realBattle.waitTillOver();
		}
	}

	public void waitTillOver() {
		if (battle != null) {
			battle.waitTillOver();
		}
	}

	private void replayBattle() {
		logMessage("Preparing replay...");
		if (battle != null && battle.isRunning()) {
			battle.stop(true);
		}

		Logger.setLogListener(battleEventDispatcher);

		recordManager.detachRecorder();
		battle = Container.factory.as(Characteristics.NO_CACHE).getComponent(BattlePlayer.class);

		Thread battleThread = new Thread(Thread.currentThread().getThreadGroup(), battle);

		battleThread.setPriority(Thread.NORM_PRIORITY);
		battleThread.setName("BattlePlayer Thread");

		// Start the battlePlayer thread
		battleThread.start();
	}

	public String getBattleFilename() {
		String filename = battleFilename;

		if (filename != null) {
			if (filename.indexOf(File.separatorChar) < 0) {
				filename = FileUtil.getBattlesDir().getName() + File.separatorChar + filename;
			}
			if (!filename.endsWith(".battle")) {
				filename += ".battle";
			}
		}
		return filename;
	}

	public void setBattleFilename(String newBattleFilename) {
		battleFilename = newBattleFilename;
	}

	public String getBattlePath() {
		if (battlePath == null) {
			battlePath = System.getProperty("BATTLEPATH");
			if (battlePath == null) {
				battlePath = "battles";
			}
			battlePath = new File(FileUtil.getCwd(), battlePath).getAbsolutePath();
		}
		return battlePath;
	}

	public void saveBattleProperties() {
		if (battleProperties == null) {
			logError("Cannot save null battle properties");
			return;
		}
		if (battleFilename == null) {
			logError("Cannot save battle to null path, use setBattleFilename()");
			return;
		}
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(battleFilename);

			battleProperties.store(out, "Battle Properties");
		} catch (IOException e) {
			logError("IO Exception saving battle properties: " + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Logger.logError(e);
				}
			}
		}
	}

	public BattleProperties loadBattleProperties() {
		BattleProperties res = new BattleProperties();
		FileInputStream in = null;

		try {
			in = new FileInputStream(getBattleFilename());
			res.load(in);
		} catch (FileNotFoundException e) {
			logError("No file " + battleFilename + " found, using defaults.");
		} catch (IOException e) {
			logError("IO Exception reading " + getBattleFilename() + ": " + e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Logger.logError(e);
				}
			}
		}
		return res;
	}

	public BattleProperties getBattleProperties() {
		if (battleProperties == null) {
			battleProperties = new BattleProperties();
		}
		return battleProperties;
	}

	public void setDefaultBattleProperties() {
		battleProperties = new BattleProperties();
	}

	public boolean isManagedTPS() {
		return isManagedTPS.get();
	}

	public void setManagedTPS(boolean value) {
		isManagedTPS.set(value);
	}

	public synchronized void addListener(IBattleListener listener) {
		battleEventDispatcher.addListener(listener);
	}

	public synchronized void removeListener(IBattleListener listener) {
		battleEventDispatcher.removeListener(listener);
	}

	public synchronized void stop(boolean waitTillEnd) {
		if (battle != null && battle.isRunning()) {
			battle.stop(waitTillEnd);
		}
	}

	public synchronized void restart() {
		// Start new battle. The old battle is automatically stopped
		startNewBattle(battleProperties, false);
	}

	public synchronized void replay() {
		replayBattle();
	}

	private boolean isPaused() {
		return (pauseCount != 0);
	}

	public synchronized void togglePauseResumeBattle() {
		if (isPaused()) {
			resumeBattle();
		} else {
			pauseBattle();
		}
	}

	public synchronized void pauseBattle() {
		if (++pauseCount == 1) {
			if (battle != null && battle.isRunning()) {
				battle.pause();
			} else {
				battleEventDispatcher.onBattlePaused(new BattlePausedEvent());
			}
		}
	}

	public synchronized void pauseIfResumedBattle() {
		if (pauseCount == 0) {
			pauseCount++;
			if (battle != null && battle.isRunning()) {
				battle.pause();
			} else {
				battleEventDispatcher.onBattlePaused(new BattlePausedEvent());
			}
		}
	}

	public synchronized void resumeIfPausedBattle() {
		if (pauseCount == 1) {
			pauseCount--;
			if (battle != null && battle.isRunning()) {
				battle.resume();
			} else {
				battleEventDispatcher.onBattleResumed(new BattleResumedEvent());
			}
		}
	}

	public synchronized void resumeBattle() {
		if (--pauseCount < 0) {
			pauseCount = 0;
			logError("SYSTEM: pause game bug!");
		} else if (pauseCount == 0) {
			if (battle != null && battle.isRunning()) {
				battle.resume();
			} else {
				battleEventDispatcher.onBattleResumed(new BattleResumedEvent());
			}
		}
	}

	/**
	 * Steps for a single turn, then goes back to paused
	 */
	public synchronized void nextTurn() {
		if (battle != null && battle.isRunning()) {
			battle.step();
		}
	}

	public synchronized void prevTurn() {
		if (battle != null && battle.isRunning() && battle instanceof BattlePlayer) {
			((BattlePlayer) battle).stepBack();
		}
	}

	public synchronized void killRobot(int robotIndex) {
		if (battle != null && battle.isRunning() && battle instanceof Battle) {
			((Battle) battle).killRobot(robotIndex);
		}
	}

	public synchronized void setPaintEnabled(int robotIndex, boolean enable) {
		if (battle != null && battle.isRunning()) {
			battle.setPaintEnabled(robotIndex, enable);
		}
	}

	public synchronized void setSGPaintEnabled(int robotIndex, boolean enable) {
		if (battle != null && battle.isRunning() && battle instanceof Battle) {
			((Battle) battle).setSGPaintEnabled(robotIndex, enable);
		}
	}

	public synchronized void sendInteractiveEvent(Event event) {
		if (battle != null && battle.isRunning() && !isPaused() && battle instanceof Battle) {
			((Battle) battle).sendInteractiveEvent(event);
		}
	}
}
