GaussSine {

	var <server, <proxy, <>pattern_name, <>id, <>param;

	classvar <n_instances = 0, <synth_name = \gauss_sine;

	*new { | server, proxy, pattern_name |
		n_instances = n_instances + 1;
		^super.newCopyArgs(server ? Server.default, proxy, pattern_name)
			.initSynth.initParam
			.initPatternBus(pattern_name);
	}

	initSynth {
		this.id = n_instances - 1;  // set unique identifier for object (used mainly for cloud_inbus array index)

		if(SynthDescLib.global.at(synth_name) == nil, {

			SynthDef(synth_name, {|out=0,freq=200,amp=0.1,grain_dur=0.01,pos=0|
				var sig = FSinOsc.ar(freq) * amp * EnvGen.ar(Env.sine(grain_dur),doneAction:2);
				OffsetOut.ar(out,Pan2.ar(sig,pos));
			}).add;

		},{
			"SynthDef already initiated".postln;
		});
	}

	initPatternBus {|pattern_name|
		this.pattern_name = (pattern_name ++ "_pattern").asSymbol;
		proxy[this.pattern_name].ar;
	}



	initParam {
		this.param = (
			instrument: synth_name,   // don't change the instrument !
			dur: 0.1,
			freq: 1000,
			amp: 0.1,
			grain_dur: 0.01,
			pos: 0
		);
	}

	// getters and setters for single parameter replacement
	// alternatively the entire param event can be replaced by a new event
	getInst { ^this.param.instrument }
	getDur { ^this.param.dur }
	getFreq { ^this.param.freq }
	getAmp { ^this.param.amp }
	getGrainDur { ^this.param.grain_dur }
	getPos { ^this.param.pos }

	setDur {|n| this.param.dur = n }
	setFreq {|n| this.param.freq = n }
	setAmp {|n| this.param.amp = n }
	setGrainDur {|n| this.param.grain_dur = n }
	setPos {|n| this.param.pos = n }


	patternBus {   // returns a reference to the proxy outbus for the pattern
		^proxy[this.pattern_name]
	}

	patternGen { |voices, fadeTime|

		proxy[this.pattern_name] = nil;         //reset pattern
		proxy[this.pattern_name].fadeTime = fadeTime;

		voices.do({ |i|
			proxy[this.pattern_name][i] = Pbind(
				\instrument, \gauss_sine,
				\dur, Pif(this.param.dur.isKindOf(Function), this.param.dur.value , this.param.dur),
				\freq, Pif(this.param.freq.isKindOf(Function), this.param.freq.value, this.param.freq),
				\amp, Pif(this.param.amp.isKindOf(Function), this.param.amp.value, this.param.amp),
				\grain_dur, Pif(this.param.grain_dur.isKindOf(Function), this.param.grain_dur.value, this.param.grain_dur),
				\pos, Pif(this.param.pos.isKindOf(Function), this.param.pos.value, this.param.pos),
			);
		});

		^proxy[this.pattern_name];

	}

}