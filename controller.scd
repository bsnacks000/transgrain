
"transgrain/scripts/install_transgrain.scd".loadRelative;

// boot and build a proxyspace
(
s.waitForBoot({
	p = ProxySpace(s);

	~sine = GaussSine(s,p,\gsine);
	~sine2 = GaussSine(s,p,\gsine2);
	~cloud = CloudEnvB(s,p,\genv);
});
)

p

(
~sine.setAmp(Pwhite(0.3,0.7));
~sine.setFreq(Pseq((100,200..1000),inf));
~sine2.setFreq(Pwhite(1000,5000));
~sine2.setDur(0.01);

~sine.patternGen(1,3);
~sine2.patternGen(1,3);
)

~sine3 = GaussSine(s,p,\gsine3)
~sine3.setDur(0.2)
~sine3.setFreq(Pwhite(9400,12900));
~sine3.patternGen(1,3)

~play_obj = (fadeTime:1, mix:0.5, room: 0.5, damp:0.5)

~cloud.connect(~sine.id, ~sine.patternBus)  // passing in id and outbus is much more efficient then passing ~sine object by value
~cloud.connect(~sine2.id, ~sine2.patternBus)
~cloud.connect(~sine3.id, ~sine3.patternBus)

~cloud.disconnect(~sine2.id)
~cloud.disconnect(~sine.id)
~cloud.disconnect(~sine3.id)


~cloud.playEnv()
~cloud.endEnv()


~cloud.setDur(0.2).playEnv()
~cloud.setSus(0.01).playEnv()
~cloud.setAtk(0.01)
~cloud.setRel(0.01)



~sine.patternGen(1,3)
~sine2.patternGen(1,3)

~cloud.setInbus(\bus);
p[\bus][~sine.id] = ~sine.outBus;
p[\bus][~sine2.id] = ~sine2.outBus;
~cloud.setInbus(\bus);


p[\out] = {FreeVerb.ar(~cloud.cloudGen(3))}
p[\out].play
p[\out].end;

// unload the module from the library
"transgrain/scripts/uninstall_transgrain.scd".loadRelative;
