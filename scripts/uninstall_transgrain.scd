// this script uninstalls TransGrain classes from the user SC class library
// it is used by calling by loading the file path in a different script (i.e. loadRelative)
{
	Quarks.uninstall("SynthModule");
	thisProcess.recompile;

}.value;
