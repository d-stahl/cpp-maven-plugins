package com.ericsson.tools.cpp.car;

import com.ericsson.tools.cpp.tools.AbstractHelpMojo;

/**
 * Display help information on car-maven-plugin.
 *
 * @goal help
 * @requiresProject false
 * @threadSafe
 */
public class HelpMojo extends AbstractHelpMojo {
	/**
	 * If <code>true</code>, display all properties for any described goal.
	 * 
	 * @parameter expression="${detail}" default-value="false"
	 */
	private boolean detail;

	/**
	 * The name of the goal for which to show help. If unspecified, all goals will be displayed.
	 * 
	 * @parameter expression="${goal}"
	 */
	private String goal;
	
	@Override
	protected boolean printGoal(final String goal) {
		if( goal.equals("car") ) {
			printCarGoal();
			return true;
		}
		
		if( goal.equals("help") ) {
			printHelpGoal();
			return true;
		}
		
		return false;
	}

	@Override
	protected void printAllGoals() {
		getLog().info("This plugin has 2 goals:");
		printCarGoal();
		printHelpGoal();
	}

	@Override
	protected void printIntroduction() {
		getLog().info("CAR Plugin");
		getLog().info("This plugin packages CAR (C/C++ Archive) artifacts to be consumed as dependencies by the cpp-compiler-maven-plugin.");
		getLog().info("The cpp-compiler-maven-plugin declares a \"car\" lifecycle in which this plugin is invoked in the package phase.");
		getLog().info("");

		getLog().info("To view one specific goal, please use the -Dgoal=<goal> parameter.");
		getLog().info("To view all available goals, omit the -Dgoal parameter.");
		getLog().info("To view detailed information, including available parameters, please use the -Ddetail=true parameter.");
		getLog().info("");
}
	
	@Override
	protected boolean getDetail() {
		return detail;
	}

	@Override
	protected String getGoal() {
		return goal;
	}
	
	private void printCarGoal() {
		getLog().info("  car:      The default behavior (as when invoked as part of the car lifecycle) is to package the contents of the output directory into a main artifact.");
		getLog().info("            Any built libraries or executables are then packaged as attached artifacts.");
		getLog().info("            Custom attached artifacts can be created by setting the directory and classifier plugin parameters.");
		getLog().info("");

		if( getDetail() ) {
			printCarParameters();
			getLog().info("");
		}

	}

	private void printCarParameters() {
		getLog().info("    classifier:  The classifier to to attach to the artifact.");
		getLog().info("    directory:   The directory to archive, if other than the standard output directory.");
	}
}
