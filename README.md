# TransGrain
### current version 0.0.1

TransGrain is a modular granular synthesis API written for SuperCollider and using the JIT framework for live coding. Its purpose is to create a more intuitive interface for producing complex granular clouds and patterns on the fly.

## Implementation
There are two types of classes in the TransGrain API, Enevelopes and Grain Generators. Both are constructed client side and are loaded into a ProxySpace. Quarks are located in the `classes` folder and can be installed to the user's SC build with a Quarks.install command or using the install and uninstall scd files in the `scripts` folder.

After the classes are loaded we boot a server, instantiate and proxyspace and load these into proxy objects.
```
(
s.waitForBoot({
	p = ProxySpace(s)
	~sine = GaussSine(s,p,\gsine);
	~cloud = CloudEnv(s,p,\genv);
});
)
```
The object's setter methods except constants, array method values and Pattern objects such as Pseq and Prand. 
Each individual parameter can be set
```
~sine.setAmp(Pwhite(0.3,0.7));
~sine.setFreq(Pseq((100,200..1000),inf));
```
or the entire param event can be set all at once.
```
~sine.param = (dur: 0.1, freq: 1000, amp: 0.1, grain_dur: 0.01, pos: 0)
```
After its set a the `.patternGen method` is called with number of voices and fadeTime. The parameters are evaluated and start the proxy starts in the background. 
```
~sine.patternGen(1,3);
```

Audio output is controlled by a cloud envelope not the `~sine` object. To hear output we connect to a cloud envelope, set cloud envelope parameters and finally play the sound 
```
~cloud.connect(~sine.id, ~sine.patternBus)
~cloud.setDur(0.2)
~cloud.setSus(0.01)
~cloud.setAtk(0.01)
~cloud.setRel(0.01)

~cloud.playEnv
```
TransGrain envelopes are patchable so multiple grain generator outputs can be connected and disconnected from instances of different cloud envelopes using the `~sine.patternBus`
```
~sine2.setFreq(Pwhite(1000,5000));
~sine2.setDur(0.01);
~sine2.patternGen(1,3);

~cloud.connect(~sine2.id, ~sine2.patternBus)

```
Now `~sine` and `~sine2` are on the same cloud envelope. They can be disconnected with `~cloud.disconnect`. The cloud envelope proxy can be stopped with `~cloud.endEnv()`

```
~cloud.disconnect(~sine2.id)
~cloud.disconnect(~sine.id)

~cloud.endEnv
```





