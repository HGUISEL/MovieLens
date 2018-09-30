package edu.handong.csee.pp1.bigdata.movielens;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.handong.csee.pp1.bigdata.movielens.Main;

public class MainTest {

	@Test
	public void testGUI() {
		String[] args = {"-d"};
        Main.main(args);
	}
	
	@Test
	public void testHelp() {
		String[] args = {"-h"};
        Main.main(args);
	}
	
	@Test
	public void testMainWithDefault() {
		String[] args = {""};
        Main.main(args);
	}
}
