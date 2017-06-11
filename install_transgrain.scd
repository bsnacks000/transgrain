{
	var localpath = thisProcess.nowExecutingPath.dirname +/+ "transgrain"; // add this as a local path for install
	Quarks.install(localpath +/+ "SynthModule"); // now install each Module seperately
	thisProcess.recompile;

}.value;
