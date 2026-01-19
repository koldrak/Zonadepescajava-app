package com.daille.zonadepescajava_app.data;

public class AudioSettings {

    private final float musicVolume;
    private final float sfxVolume;
    private final boolean musicEnabled;
    private final boolean sfxEnabled;

    public AudioSettings(float musicVolume, float sfxVolume, boolean musicEnabled, boolean sfxEnabled) {
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.musicEnabled = musicEnabled;
        this.sfxEnabled = sfxEnabled;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
}
