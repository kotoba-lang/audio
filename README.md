# kotoba-lang/audio

KAMI Audio — zero-dep portable `.cljc`, restored from the legacy
`kami-engine/kami-audio` Rust crate (537 lines across `src/lib.rs`,
`src/binaural.rs`, `src/wav.rs`; deleted in kami-engine PR #82 "Remove
Rust workspace from kami-engine") as part of the **clj-wgsl migration**
(ADR-2607010930, `com-junkawasaki/root`).

Ledger class `:port-to-WGSL-compute` ("mixer loop + voice alloc + SR
buffer mgmt; emit :native stays for real-time latency"). Per owner
decision this is restored as a plain interim CLJC port of the pure
mixer / voice-allocation / binaural-spatialization data and math — not
actual WGSL compute code, and not real-time audio-hardware-callback
code. The original crate had no literal hardware-callback code to
exclude: `AudioMixer::spatialize` / `active_voices`, the binaural DSP,
and the WAV encoder are all pure functions over plain data/buffers, so
the port is complete (nothing was intentionally left out).

## Modules

- **`src/audio.cljc`** (from `src/lib.rs`) — `AudioSource` / `Listener`
  / `AudioMixer`: 3D positional audio sources, distance attenuation
  (`:linear` / `:inverse` / `:exponential` rolloff), a simplified
  stereo pan (dot product against the listener's right vector), a
  per-channel + master volume mix, and priority-based voice limiting
  (`active-voices`).
- **`src/audio/binaural.cljc`** (from `src/binaural.rs`) — physically
  grounded spherical-head binaural spatialization: Woodworth ITD
  (`itd = (a/c)(theta + sin theta)`), frequency-independent ILD
  head-shadow, distance rolloff (`:none` / `:inverse` / `:linear` /
  `:exponential`), and `mix-stereo` — pure software mixing of
  per-source mono buffers into an interleaved stereo buffer with
  per-ear ITD sample delay and gain.
- **`src/audio/wav.cljc`** (from `src/wav.rs`) — dependency-free 16-bit
  PCM stereo WAV encoding (`encode-pcm16-stereo`) of an interleaved
  stereo float buffer, e.g. the output of `audio.binaural/mix-stereo`.
  JVM implementation via `java.nio.ByteBuffer` (matching the
  JVM-binary-export pattern already used in `kotoba-lang/engineer-io`'s
  `engineer-io.stl/export-binary` and `kotoba-lang/pnr`'s
  `pnr.gdsii`); CLJS implementation via `js/ArrayBuffer` + `js/DataView`.

## Relationship to `kotoba-lang/rtc`

The sibling restoration `kotoba-lang/rtc`'s `rtc.spatial` namespace
implements its own self-contained, simpler stereo-pan calculation
(equal-power pan + inverse-distance rolloff) because `kami-audio` had
not yet been restored to CLJC when `rtc` was restored. The two
implementations are intentionally independent and are not unified by
this restoration — `rtc.spatial` documents its own relationship to
this crate in its namespace docstring.

## Tests

All 11 original Rust `#[test]`s (1 from `lib.rs`, 8 from `binaural.rs`,
2 from `wav.rs`) ported 1:1 to `test/audio_test.cljc`, plus the
pre-existing scaffold smoke test — **12 tests / 38 assertions, 0
failures, 0 errors**.

## Develop

```bash
clojure -M:test
```
