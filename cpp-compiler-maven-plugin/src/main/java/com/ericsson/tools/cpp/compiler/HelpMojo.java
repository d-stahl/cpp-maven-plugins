package com.ericsson.tools.cpp.compiler;

import com.ericsson.tools.cpp.tools.AbstractHelpMojo;

/**
 * Display help information on cpp-compiler-maven-plugin.
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
		if( goal.equals("compile") ) {
			printCompileGoal();
			return true;
		}
		
		if( goal.equals("test-compile") ) {
			printTestCompileGoal();
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
		getLog().info("This plugin has 3 goals:");
		printCompileGoal();
		printTestCompileGoal();
		printHelpGoal();
	}

	@Override
	protected void printIntroduction() {
		getLog().info("CPP Compiler Plugin");
		getLog().info("A plugin for multi-target C/C++ builds. This plugin also defines the default car lifecycle, invoking the CPP Tester Plugin and the CAR Plugin.");
		getLog().info("The plugin will produce one main artifact containing header files and other resources, and attached artifacts for static and shared libraries. These in turn contain one set of libraries for each target.");
		getLog().info("Any extant libs in <source>/<main or test>/lib/static and <source>/<main or test>/lib/shared will be included in static and shared attached artifacts respectively.");
		getLog().info("");

		if( getDetail() ) {
			printDependencyExtractionExplanation();
			getLog().info("");
			printEnvironmentExplanation();
			getLog().info("");
			printBundleExplanation();
			getLog().info("");
		}

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
	
	private void printTestCompileGoal() {
		getLog().info("  test-compile: Compiles, archives and links test sources.");
		getLog().info("                Code in <source>/test/cpp, along with headers in <source>/test/include, will be included in the build. <source> is controlled by the sources parameter.");
		getLog().info("");

		if( getDetail() ) {
			printCommonParameters();
			printTestCompileParameters();
			getLog().info("");
		}
	}

	private void printCompileGoal() {
		getLog().info("  compile:      Compiles, archives and links sources.");
		getLog().info("                Code in <source>/main/cpp, along with headers in <source>/main/include, will be included in the build. <source> is controlled by the sources parameter.");
		getLog().info("");

		if( getDetail() ) {
			printCommonParameters();
			printCompileParameters();
			getLog().info("");
		}
	}

	private void printBundleExplanation() {
		getLog().info("  The plugin itself doesn't contain any target specific build logic. Instead it relies on \"bundles\" (think of them as plugins to the plugin) to build in a given host environment for a given target.");
		getLog().info("  Bundles are defined as plugin dependencies. The plugin will then ask its bundles whether they are compatible with the execution environment and can build for the target environment.");
		getLog().info("  As an example, a project that specifies win_x86_64 as target may have a bundle relying on msvc.exe as a dependency.");
		getLog().info("  Such a plugin would typically report that it can get the job done when and only when building in Windows.");
		getLog().info("  Plugin dependencies are specified as ordinary dependencies, except they're defined within the <plugin></plugin> tags.");
	}

	private void printDependencyExtractionExplanation() {
		getLog().info("  \"car\" dependency artifacts are lazily extracted to \"target/extractedDependencies\".");
		getLog().info("  The contents of this directory are then used for compilation and linking.");
		getLog().info("  To ensure the currency of the extracted dependencies the entire target directory is cleaned if any update is detected either in the POM or any SNAPSHOT parent POM.");
		getLog().info("  The extracted files of any SNAPSHOT dependency are compared to the latest SNAPSHOT artifact in the local repository and updated if necessary.");
		getLog().info("  A side effect of this is that all dependency headers are available in the project and can be used for indexing in the IDE.");
		getLog().info("  All extracted dependencies are write protected to keep the developer from accidentally editing them.");
	}
	
	private void printEnvironmentExplanation() {
		getLog().info("  Environments are a key concept in the CPP plugin suite. Each build is performed in a specific host environment to a specific target environment.");
		getLog().info("  Each environment is identified by a canonical name, and possibly by a number of aliases. These may be defined by bundles, but some common environments are defined by the plugin itself.");
		getLog().info("  These predefined environments are: linux_32, linux_64, win_32, win_64, solaris_32, solaris_64, solaris_sparc.");
		getLog().info("  Note that this does not mean that the plugin by itself supports these environments. It still requires appropriate bundles.");
	}
	
	private void printCommonParameters() {
		getLog().info("    linkerArguments: A map of linker arguments, with targets as keys.");
		getLog().info("        The \"all\" key is applied to all targets.");
		getLog().info("        Example:");
		getLog().info("          <linkerArguments>");
		getLog().info("            <all>-lmylib</all>");
		getLog().info("            <linux_x86>-lsomelinuxlib</linux_x86>");
		getLog().info("          </linkerArguments>");
		getLog().info("        Default:");
		getLog().info("          <linkerArguments>");
		getLog().info("            <all>-lpthread -lm -stdc++</all>");
		getLog().info("          </linkerArguments>");
		getLog().info("        Individual keys may be specified independently, but not appended.");
		getLog().info("        In other words, a parent may specify \"all\", while its child specifies \"myTarget\".");
		getLog().info("    compilerArguments: A map of compiler arguments, with targets as keys.");
		getLog().info("        compilerArguments work similarly to linkerArguments.");
		getLog().info("        Example:");
		getLog().info("          <compilerArguments>");
		getLog().info("            <all>-O3</all>");
		getLog().info("            <linux_x86>-DMYDEF</linux_x86>");
		getLog().info("          </compilerArguments>");
		getLog().info("    sources: A map of source directories.");
		getLog().info("        This map can be used to control the location of sources.");
		getLog().info("        It can also be used to map multiple target sources to one directory.");
		getLog().info("        By default common sources are expected in \"src\", with target specific sources in \"src/<target>\".");
		getLog().info("        Example:");
		getLog().info("          <sources>");
		getLog().info("            <all>src/common</all>");
		getLog().info("            <linux_x86>src/linux</linux_x86>");
		getLog().info("            <linux_x86_64>src/linux</linux_x86_64>");
		getLog().info("          </sources>");
	}

	private void printTestCompileParameters() {
		getLog().info("    testExecutables: See \"executables\" in compile parameters.");
	}

	private void printCompileParameters() {
		getLog().info("    executables: Specifies one or more executables to link.");
		getLog().info("        Example:");
		getLog().info("          <executables>");
		getLog().info("            <executable>");
		getLog().info("              <name>myExec</name>");
		getLog().info("              <sourcesPattern>src/main/cpp/main.cc</sourcesPattern>");
		getLog().info("              <targets>linux_32</targets>");
		getLog().info("              <rpath>-rpath,\\&ORIGIN/lib</rpath>");
		getLog().info("            </executable>");
		getLog().info("            <executable>");
		getLog().info("              <name>[]</name>");
		getLog().info("            </executable>");
		getLog().info("          </executables>");
		getLog().info("        Only name is mandatory, with special name \"[]\" is expanded to one executable for each soruce file matching the sourcesPattern.");
		getLog().info("        Default sourcesPattern: src/main/cpp/*.cpp");
		getLog().info("        Default targets: all (\"all\" will be created for every target).");
		getLog().info("        Default rpath: -rpath,\\$ORIGIN");
	}
}
