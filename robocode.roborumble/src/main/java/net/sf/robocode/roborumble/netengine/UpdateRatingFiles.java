/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.roborumble.netengine;


import static net.sf.robocode.roborumble.util.PropertiesUtil.getProperties;
import static net.sf.robocode.roborumble.util.PropertiesUtil.storeProperties;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;


/**
 * Class used for updating the local rating files.
 * Controlled by properties files.
 *
 * @author Albert Perez (original)
 * @author Flemming N. Larsen (contributor)
 * @author Pavel Savara (contributor)
 */
public class UpdateRatingFiles {

	private final String game;
	private final String minibots;
	private final String microbots;
	private final String nanobots;
	private final String battlesnumfile;
	private final String generalratings;
	private final String miniratings;
	private final String microratings;
	private final String nanoratings;

	public UpdateRatingFiles(String game, Properties parameters) {
		this.game = game;
		minibots = parameters.getProperty("MINIBOTS", "");
		microbots = parameters.getProperty("MICROBOTS", "");
		nanobots = parameters.getProperty("NANOBOTS", "");

		battlesnumfile = parameters.getProperty("BATTLESNUMFILE", "");

		generalratings = parameters.getProperty("RATINGS.GENERAL", "");
		miniratings = parameters.getProperty("RATINGS.MINIBOTS", "");
		microratings = parameters.getProperty("RATINGS.MICROBOTS", "");
		nanoratings = parameters.getProperty("RATINGS.NANOBOTS", "");
	}

	public boolean updateRatings() {

		// read all the records to be updated
		Vector<String> battles = new Vector<String>();
		BufferedReader br = null;

		try {
			FileReader fr = new FileReader(battlesnumfile);

			br = new BufferedReader(fr);
			String record;

			while ((record = br.readLine()) != null) {
				battles.add(record);
			}
		} catch (IOException e) {
			System.out.println("Can't open # battles file ... Aborting # battles update");
			return false;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ignored) {}
			}
		}

		// read the ratings files
		Properties all = getProperties(generalratings);
		Properties mini = getProperties(miniratings);
		Properties micro = getProperties(microratings);
		Properties nano = getProperties(nanoratings);

		// update #battles
		for (String battle : battles) {
			String[] battleSpec = battle.split(",");

			battleSpec[1] = battleSpec[1].replaceAll(" ", "_");
			double num = Double.parseDouble(battleSpec[2]);

			if (battleSpec[0].equals(game)) {
				updateRecord(battleSpec[1], num, all);
			} else if (battleSpec[0].equals(minibots) && mini != null) {
				updateRecord(battleSpec[1], num, mini);
			} else if (battleSpec[0].equals(microbots) && micro != null) {
				updateRecord(battleSpec[1], num, micro);
			} else if (battleSpec[0].equals(nanobots) && nano != null) {
				updateRecord(battleSpec[1], num, nano);
			}
		}

		// save ratings files
		return storeProperties(all, generalratings, "General ratings updated with new battles number")
				&& storeProperties(all, miniratings, "Mini ratings updated with new battles number")
				&& storeProperties(all, microratings, "Micro ratings updated with new battles number")
				&& storeProperties(all, nanoratings, "Nano ratings updated with new battles number");
	}

	private void updateRecord(String bot, double battles, Properties ratings) {
		String values = ratings.getProperty(bot);

		if (values == null) {
			return;
		}

		String[] value = values.split(",");

		values = value[0] + "," + battles + "," + value[2];
		ratings.setProperty(bot, values);
	}
}
