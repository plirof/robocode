/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package robocode.robocodeGL.system;


import robocode.robocodeGL.RenderElement;


/**
 * Dummy class that stub out RobocodeGL 0.1.4 functionality.
 *
 * Original author is David Alves for RobocodeGL.
 */
public class GLRenderer {

	public static GLRenderer getInstance() {
		return new GLRenderer();
	}

	public void addRenderElement(RenderElement e) {}
}
