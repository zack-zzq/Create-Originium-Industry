package com.mealuet.create_originium_industry.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * Looping factory SFX with a short fade so reduce-flicker users are not hit
 * with hard on/off edges.
 */
public final class LoopingIndustrialSound extends AbstractTickableSoundInstance {

    private float targetVolume;
    private float fadeSpeed;
    private boolean keepPlaying = true;

    public LoopingIndustrialSound(SoundEvent event, SoundSource source, boolean relative, double x, double y, double z) {
        super(event, source, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.relative = relative;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fadeSpeed = 0.08F;
        this.attenuation = relative ? SoundInstance.Attenuation.NONE : SoundInstance.Attenuation.LINEAR;
    }

    public void setFadeSpeed(float fadeSpeed) {
        this.fadeSpeed = Math.max(0.01F, fadeSpeed);
    }

    public void setTarget(float volume, double x, double y, double z) {
        this.targetVolume = Mth.clamp(volume, 0.0F, 1.0F);
        if (!this.relative) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public void requestStop() {
        this.keepPlaying = false;
        this.targetVolume = 0.0F;
    }

    @Override
    public void tick() {
        if (!this.keepPlaying && this.volume <= 0.001F) {
            this.stop();
            return;
        }
        if (this.volume < this.targetVolume) {
            this.volume = Math.min(this.targetVolume, this.volume + this.fadeSpeed);
        } else {
            this.volume = Math.max(this.targetVolume, this.volume - this.fadeSpeed);
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
