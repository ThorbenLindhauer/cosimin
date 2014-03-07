/*
 * Copyright 2014 Sebastian Kruse, Thorben Lindhauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unipotsdam.hpi.util;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;


public class SettingsTest {
	
	private static class TestSettings extends AbstractSettings {

		@Property("integer")
		int i;
		
		@Property("bool")
		boolean b;
		
		@Property("double")
		double d;
		
		@Property("string")
		String s;
	}
	
	private static class SubTestSettings extends TestSettings {
	}
	
	@Test
	public void testLoading() {
		TestSettings settings = new TestSettings();
		settings.load("src/test/resources/test.properties");
		Assert.assertEquals(54321, settings.i);
		Assert.assertEquals(0.8, settings.d, 0);
		Assert.assertEquals(true, settings.b);
		Assert.assertEquals("test", settings.s);
	}
	
	@Test
	public void testExport() {
		TestSettings origSettings = new TestSettings();
		origSettings.load("src/test/resources/test.properties");
		Properties export = origSettings.toProperties();
		TestSettings copiedSettings = new TestSettings();
		copiedSettings.load(export);
		
		Assert.assertEquals(origSettings.i, copiedSettings.i);
		Assert.assertEquals(origSettings.d, copiedSettings.d, 0);
		Assert.assertEquals(origSettings.b, copiedSettings.b);
		Assert.assertEquals(origSettings.s, copiedSettings.s);
	}
	
	@Test
	public void testLoadingWithSuperclassProperties() {
		TestSettings settings = new SubTestSettings();
		settings.load("src/test/resources/test.properties");
		Assert.assertEquals(54321, settings.i);
		Assert.assertEquals(0.8, settings.d, 0);
		Assert.assertEquals(true, settings.b);
		Assert.assertEquals("test", settings.s);
	}

}
