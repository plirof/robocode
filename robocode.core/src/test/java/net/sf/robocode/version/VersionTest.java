/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.version;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;


/**
 * @author Pavel Savara (original)
 */
public class VersionTest {
	static {
		if (!new File("").getAbsolutePath().endsWith("robocode.core")) {
			throw new Error("Please run test with current directory in 'robocode.core'");
		}
	}

	@Test
	public void same() {
        Assert.assertEquals(0, Version.compare("1.1.3", "1.1.3"));
	}

	@Test
	public void equalVersion() {
        Assert.assertEquals(0, Version.compare("1.1", "1.1.0.0"));
	}

	@Test
	public void equalAlphas() {
        Assert.assertEquals(0, Version.compare(" 1.2 Alpha", "1.2.0.0alpha1 "));
	}

	@Test
	public void equalBetas() {
        Assert.assertEquals(0, Version.compare("4.2.1Beta2 ", "  4.2.1.0 beta 2"));
	}

	@Test
	public void greater() {
		Assert.assertTrue(Version.compare("1.1.4", "1.1.3") > 0);
	}

	@Test
	public void greaterShort() {
		Assert.assertTrue(Version.compare("1.4", "1.1.3") > 0);
	}

	@Test
	public void greaterShort2() {
		Assert.assertTrue(Version.compare("1.4", "1.1") > 0);
	}

	@Test
	public void greaterShortBeta() {
		Assert.assertTrue(Version.compare("1.4 Beta", "1.1.3") > 0);
	}

	@Test
	public void greaterShortBeta3() {
		Assert.assertTrue(Version.compare("1.4 Beta", "1.1") > 0);
	}

	@Test
	public void greaterShortBeta2() {
		Assert.assertTrue(Version.compare("1.4", "1.1.3") > 0);
	}

	@Test
	public void greaterLong() {
		Assert.assertTrue(Version.compare("1.4.1", "1.1") > 0);
	}

	@Test
	public void smaller() {
		Assert.assertTrue(Version.compare("1.1.2", "1.1.3") < 0);
	}

	@Test
	public void greaterToo() {
		Assert.assertTrue(Version.compare("4.1.2", "1.1.3") > 0);
	}

	@Test
	public void greaterThanBeta() {
		Assert.assertTrue(Version.compare("1.1.3", "1.1.3 Beta") > 0);
	}

	@Test
	public void smallerThanBeta() {
		Assert.assertTrue(Version.compare("1.1.2", "1.1.3 Beta") < 0);
	}

	@Test
	public void smallerThanAlpha() {
		Assert.assertTrue(Version.compare("1.1.3 Alpha", "1.1.3 Beta") < 0);
	}

	@Test
	public void compareBetas() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.1.3 Beta", "1.1.3 Beta 2") < 0);
	}

	@Test
	public void compareBetas2() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.1.3 Beta 2 ", "1.1.3 Beta 3") < 0);
	}

	@Test
	public void compareBetas3() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.1.3 Beta2 ", "1.1.3 Beta 3") < 0);
	}

	@Test
	public void compareBetas4() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.1.3Beta2", "1.1.3 Beta 3") < 0);
	}

	@Test
	public void compareBetasShort() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.7 Beta", "1.1") > 0);
	}

	@Test
	public void compareBetasShort2() {
		// we can't name versions Beta 2
		Assert.assertTrue(Version.compare("1.7 Beta", "1.8") < 0);
	}
}
