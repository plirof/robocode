/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package robocode;


/**
 * @author Pavel Savara (original)
 */
public class BadNamespace extends Robot {
	@Override
	public void run() {
		while (true) {
			ahead(1); // Move ahead 100
			turnGunRight(360); // Spin gun around
			back(1); // Move back 100
			turnGunRight(360); // Spin gun around
		}
	}
}
