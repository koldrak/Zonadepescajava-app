package com.daille.zonadepescajava_app.data;

public class AudioSettings {

    private final float musicVolume;
    private final float sfxVolume;
    private final float buttonVolume;
    private final boolean musicEnabled;
    private final boolean sfxEnabled;
    private final boolean buttonEnabled;

    public AudioSettings(float musicVolume, float sfxVolume, float buttonVolume,
                         boolean musicEnabled, boolean sfxEnabled, boolean buttonEnabled) {
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.buttonVolume = buttonVolume;
        this.musicEnabled = musicEnabled;
        this.sfxEnabled = sfxEnabled;
        this.buttonEnabled = buttonEnabled;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public float getButtonVolume() {
        return buttonVolume;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public boolean isButtonEnabled() {
        return buttonEnabled;
    }
}
