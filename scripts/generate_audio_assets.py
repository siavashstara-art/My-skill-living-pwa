import wave
import math
import struct
import random

SAMPLE_RATE = 22050

def generate_calm_harmony(filename, duration=12.0):
    num_samples = int(SAMPLE_RATE * duration)
    # Chord progression: Am -> F -> C -> G (3s each)
    chords = [
        [220.0, 261.63, 329.63, 440.0],  # Am
        [174.61, 220.0, 261.63, 349.23], # F
        [130.81, 164.81, 196.0, 261.63], # C
        [196.0, 246.94, 293.66, 392.0]   # G
    ]
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        
        for i in range(num_samples):
            t = i / SAMPLE_RATE
            chord_idx = int((t / duration) * 4) % 4
            chord = chords[chord_idx]
            
            # Subtly blend tones
            sample = 0.0
            for freq in chord:
                # Add fundamental and soft overtone with gentle LFO
                lfo = 1.0 + 0.08 * math.sin(2 * math.pi * 0.3 * t)
                tone = math.sin(2 * math.pi * freq * t) + 0.35 * math.sin(4 * math.pi * freq * t)
                sample += tone * lfo
            sample = sample / (len(chord) * 1.5)
            
            # Envelope (fade in 1s, fade out 1s)
            env = 1.0
            if t < 1.0:
                env = t
            elif t > duration - 1.0:
                env = duration - t
            sample *= env * 0.4
            
            val = max(-32767, min(32767, int(sample * 32767)))
            wav_file.writeframes(struct.pack('<h', val))

def generate_ambient_rain(filename, duration=10.0):
    num_samples = int(SAMPLE_RATE * duration)
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        
        # Simple IIR low pass filter on white noise
        last_val = 0.0
        for i in range(num_samples):
            t = i / SAMPLE_RATE
            noise = random.uniform(-1.0, 1.0)
            # Low-pass filter smoothing for soft pink/rain tone
            last_val = last_val * 0.94 + noise * 0.06
            
            # Gentle ebb and flow
            swell = 0.7 + 0.3 * math.sin(2 * math.pi * 0.2 * t)
            sample = last_val * swell * 0.35
            
            # Fade in/out
            if t < 0.8:
                sample *= (t / 0.8)
            elif t > duration - 0.8:
                sample *= (duration - t) / 0.8
                
            val = max(-32767, min(32767, int(sample * 32767)))
            wav_file.writeframes(struct.pack('<h', val))

def generate_intro_chime(filename, duration=3.5):
    num_samples = int(SAMPLE_RATE * duration)
    notes = [523.25, 659.25, 783.99, 1046.50] # C5, E5, G5, C6
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        
        for i in range(num_samples):
            t = i / SAMPLE_RATE
            sample = 0.0
            for idx, freq in enumerate(notes):
                note_start = idx * 0.25
                if t >= note_start:
                    note_t = t - note_start
                    env = math.exp(-3.5 * note_t)
                    sample += math.sin(2 * math.pi * freq * note_t) * env
            sample = sample * 0.35
            val = max(-32767, min(32767, int(sample * 32767)))
            wav_file.writeframes(struct.pack('<h', val))

def generate_page_turn(filename, duration=0.6):
    num_samples = int(SAMPLE_RATE * duration)
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        
        last = 0.0
        for i in range(num_samples):
            t = i / SAMPLE_RATE
            noise = random.uniform(-1.0, 1.0)
            last = last * 0.85 + noise * 0.15
            # Rapid rise and fall
            env = math.sin(math.pi * (t / duration)) ** 2
            sample = last * env * 0.4
            val = max(-32767, min(32767, int(sample * 32767)))
            wav_file.writeframes(struct.pack('<h', val))

if __name__ == '__main__':
    generate_calm_harmony("app/src/main/res/raw/bg_calm_harmony.wav")
    generate_ambient_rain("app/src/main/res/raw/bg_gentle_rain.wav")
    generate_intro_chime("app/src/main/res/raw/fx_intro_chime.wav")
    generate_page_turn("app/src/main/res/raw/fx_page_turn.wav")
    print("Generated 4 royalty-free studio audio assets successfully.")
