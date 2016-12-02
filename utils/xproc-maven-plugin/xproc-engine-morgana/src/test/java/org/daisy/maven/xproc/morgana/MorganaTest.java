package org.daisy.maven.xproc.morgana;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MorganaTest {
	
	@Test
	public void testSimplePipeline() throws FileNotFoundException, XProcExecutionException {
		XProcEngine engine = new Morgana();
		File resourcesDir = new File(MorganaTest.class.getResource("/").getPath());
		File pipeline = new File(resourcesDir, "foo.xpl");
		File input = new File(resourcesDir, "hello.xml");
		File expected = new File(resourcesDir, "hello_foo_expected.xml");
		File tmpDir = Files.createTempDir();
		tmpDir.deleteOnExit();
		File output = new File(tmpDir, "out.xml");
		engine.run(pipeline.toURI().toASCIIString(),
		           ImmutableMap.of("source", Arrays.asList(new String[]{input.toURI().toASCIIString()})),
		           ImmutableMap.of("result", output.toURI().toASCIIString()),
		           null,
		           null);
		assertEquals(readFileContents(expected),
		             readFileContents(output));
	}
	
	private static String readFileContents(File file) throws FileNotFoundException {
		try {
			return new Scanner(file).useDelimiter("\\Z").next(); }
		catch (NoSuchElementException e) {
			return ""; }
	}
}
