/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.ui;


import net.sf.robocode.ui.gfx.RenderImage;

import java.awt.*;


/**
 * @author Pavel Savara (original)
 */
public interface IImageManager {
	void initialize();

	Image getGroundTileImage(int index);

	RenderImage getExplosionRenderImage(int which, int frame);

	RenderImage getExplosionDebriseRenderImage();

	RenderImage getColoredBodyRenderImage(Integer color);

	RenderImage getColoredGunRenderImage(Integer color);

	RenderImage getColoredRadarRenderImage(Integer color);
}
