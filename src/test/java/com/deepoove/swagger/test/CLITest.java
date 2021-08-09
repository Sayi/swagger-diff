package com.deepoove.swagger.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.deepoove.swagger.diff.cli.CLI;

public class CLITest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(System.out);
    }

    @Test
    public void testCLI() {
        CLI cli = new CLI();
        String[] argv = { "-v", "2.0", "-old", "http://petstore.swagger.io/v2/swagger.json",
                "--help" };
        JCommander commander = JCommander.newBuilder().addObject(cli).build();

        commander.setProgramName("java swagger-diff.jar");
        commander.usage();
        commander.parse(argv);
        Assert.assertEquals(cli.getVersion(), "2.0");
        Assert.assertEquals(cli.getOldSpec(), "http://petstore.swagger.io/v2/swagger.json");
    }

    @Test
    public void testRegex() {
        CLI cli = new CLI();
        String[] argv = { "--help", "-v", "2.0", "-output-mode", "markdown" };
        JCommander.newBuilder().addObject(cli).build().parse(argv);

        argv = new String[] { "--help", "-v", "1.0", "-output-mode", "html" };
        JCommander.newBuilder().addObject(cli).build().parse(argv);

        argv = new String[] { "--help", "-v", "1.1.0" };
        try {
            JCommander.newBuilder().addObject(cli).build().parse(argv);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(e instanceof ParameterException);
        }

        argv = new String[] { "--help", "-output-mode", "html5" };
        try {
            JCommander.newBuilder().addObject(cli).build().parse(argv);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assert.assertTrue(e instanceof ParameterException);
        }
    }

    @Test
    public void testHelp() {
        CLI cli = new CLI();
        String[] argv = { "--help" };
        JCommander jCommander = JCommander.newBuilder().addObject(cli).build();
        jCommander.parse(argv);
        cli.run(jCommander);
        Assert.assertTrue(outContent.toString().startsWith("Usage: java -jar swagger-diff.jar "));
    }
    
    @Test
    public void testVersion() {
        CLI cli = new CLI();
        String[] argv = { "--version" };
        JCommander jCommander = JCommander.newBuilder().addObject(cli).build();
        jCommander.parse(argv);
        cli.run(jCommander);
        Assert.assertEquals(outContent.toString().trim(), "1.2.2");
    }
    
    @Test
    public void testMain() {
        CLI cli = new CLI();
        String[] argv = { "-old", "petstore_v2_1.json", "-new", "petstore_v2_2.json" };
        JCommander jCommander = JCommander.newBuilder().addObject(cli).build();
        jCommander.parse(argv);
        cli.run(jCommander);
        Assert.assertTrue(outContent.toString().startsWith("## Version 1.0.0 to 1.0.2"));
    }

}
