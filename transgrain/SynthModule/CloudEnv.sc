CloudEnvB {

	var <server, <proxy, <>inbus_name, <>outbus_name, <>env_name, <>id, <>param;

	classvar <n_instances = 0, <synth_name = \cloud_env;

	*new { | server, proxy, env_name |
		n_instances = n_instances + 1;
		^super.newCopyArgs(server ? Server.default, proxy, env_name)
			.initSynth
			.initEnvProxy(env_name)
			.initInBusProxy(env_name)
			.initOutBusProxy(env_name)
			.initParam;
	}

	initSynth {
		this.id = n_instances -1;

		if(SynthDescLib.global.at(synth_name) == nil, {

			//ADSR cloud event envelope - fixed length - use with PatternProxy
			SynthDef(synth_name, {|out=0,inbus,sus_lvl=0.75,atk=0.1,dec=0.1,sus=1,rel=1|
				var env = Env([0.001,1,sus_lvl,sus_lvl,0.001],[atk,dec,sus,rel],\exp); // 4 stage adsr fixed length
				Out.ar(out, In.ar(inbus, 2) * EnvGen.kr(env,doneAction:2)); // Julian's fix... In.ar(inbus)
			}).add;

		},{
			"SynthDef already initiated".postln;
		});
	}


	initEnvProxy { |env_name|
		this.env_name = env_name; proxy[this.env_name].ar;

	}

	initInBusProxy { |env_name |
		this.inbus_name = (env_name ++ "_inbus").asSymbol;  // make proxy name
		proxy[this.inbus_name].ar;                // build inbus in proxyspace

	}

	initOutBusProxy { |env_name|
		this.outbus_name = (env_name ++ "_out").asSymbol;
		proxy[this.outbus_name].ar;

	}


	initParam {
		this.param = (
			instrument: synth_name,   // don't change the instrument !
			inbus: proxy[this.inbus_name].index,      // don't change the inbus !
			sus_lvl: 0.25,
			dur: 5,
			atk: 0.01,
			dec: 0.12,
			sus: 1,
			rel: 3
		);
	}

	// individual getters and setters for cloud_env
	getInst { ^this.param.instrument }
	getInbus { ^this.param.inbus }
	getSusLvl { ^this.param.sus_lvl }
	getDur { ^this.param.dur }
	getAtk { ^this.param.atk }
	getDec { ^this.param.dec }
	getSus { ^this.param.sus }
	getRel { ^this.param.rel }

	//setInbus {|n| this.inbus_name = n; this.param.inbus = proxy[this.inbus_name].index;}
	setSusLvl {|n| this.param.sus_lvl = n }
	setDur { |n| this.param.dur = n }
	setAtk { |n| this.param.atk = n }
	setDec { |n| this.param.dec = n }
	setSus { |n| this.param.sus = n }
	setRel { |n| this.param.rel = n }

	inBus {               // returns a reference to the inbus proxy
		^proxy[this.inbus_name];
	}


	outBus {               // returns a reference to the outbus proxy
		^proxy[this.outbus_name];
	}

	cloudGen { |fadeTime = 1|          // generates the cloud process and returns an audio signal for outbus

		proxy[this.env_name].fadeTime = fadeTime;

		proxy[this.env_name] = Pbind(
			\instrument, synth_name,
			\inbus, proxy[this.inbus_name].index,

			\sus_lvl, Pif(this.param.sus_lvl.isKindOf(Function), this.param.sus_lvl.value, this.param.sus_lvl),
			\dur, Pif(this.param.dur.isKindOf(Function), this.param.dur.value , this.param.dur),
			\atk, Pif(this.param.atk.isKindOf(Function), this.param.atk.value, this.param.atk),
			\dec, Pif(this.param.dec.isKindOf(Function), this.param.dec.value, this.param.dec),
			\sus, Pif(this.param.sus.isKindOf(Function), this.param.sus.value, this.param.sus),
			\rel, Pif(this.param.rel.isKindOf(Function), this.param.rel.value, this.param.rel)
		);

		^proxy[this.env_name].ar;
	}

	// connect and disconnect take references to an grain pattern object's id and outbus
	// and set them to the CloudEnv object inbus
	// if cloudEnv is not already playing than play( if play_obj != nil then use the extra parameters)

	connect { |obj_id, obj_outbus, play_obj=nil |

		proxy[this.inbus_name][obj_id] = obj_outbus;

		if(proxy[this.outbus_name].isPlaying==false,{
			if(play_obj!=nil, {
				^this.playEnv(play_obj[\fadeTime],play_obj[\mix],play_obj[\room],play_obj[\damp] )
			});
			^this.playEnv();

		}, { ^this });
	}

	disconnect { |obj_id|
		proxy[this.inbus_name].removeAt(obj_id);

		if(proxy[this.inbus_name].sources.size == 0, {
			^this.endEnv();
		},{^this})
	}

	// set play (with FreeVerb) and end for outbus proxy
	playEnv {|fadeTime=1, mix=0.33,room=0.5,damp=0.5|
		proxy[this.outbus_name] = { FreeVerb.ar(this.cloudGen(fadeTime),mix,room,damp) };
		^proxy[this.outbus_name].play;
	}


	endEnv{
		^proxy[this.outbus_name].end;
	}


}