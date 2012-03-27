package com.ericsson.tools.cpp.tester;

import com.ericsson.tools.cpp.tools.AbstractHelpMojo;

/**
 * Display help information on cpp-tester-maven-plugin.
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
		if( goal.equals("test") ) {
			printTestGoal();
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
		printTestGoal();
		printHelpGoal();
	}

	@Override
	protected void printIntroduction() {
		getLog().info("CPP Tester Plugin");
		getLog().info("This plugin executes test executables built by the cpp-compiler-maven-plugin.");
		getLog().info("The cpp-compiler-maven-plugin declares a \"car\" lifecycle in which this plugin is invoked in the test phase.");
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
	
	private void printTestGoal() {
		getLog().info("  test:       Executes any found test executables.");
		getLog().info("");

		if( getDetail() ) {
			printTestParameters();
			getLog().info("");
		}
	}

	private void printTestParameters() {
		getLog().info("    hostEnvironment:  This parameter is required in order to locate the appropriate tests.");
		getLog().info("                      It is automatically set by the cpp-compiler-maven-plugin when run via the car lifecycle.");
		getLog().info("                      When executed independently of the car lifecycle the CPP Tester Plugin requires the parameter to be explicity defined, either via the POM or by setting the host.environment system property.");
		getLog().info("    runValgrind:      Whether to execute Valgrind. If true, Valgrind is expected to be present on the path.");
		getLog().info("    suppressionsFile: File containing Valgrind suppressions. Only used if runValgrind is true."); 
		getLog().info("                      Default: ${basedir}/src/test/cpp/valgrind.supp");
	}
}
