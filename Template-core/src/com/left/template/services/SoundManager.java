package com.left.template.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.left.template.utils.LRUCache;
import com.left.template.utils.Res;
import com.left.template.utils.LRUCache.CacheEntryRemovedListener;

public class SoundManager implements Disposable, CacheEntryRemovedListener<SoundManager.SoundList, Sound> {

	public enum SoundList {
		NONE(null),
		TEST("test.mp3"),
		CLICK("click.wav"),
		LAYROAD("layroad.mp3");
		
		private final String fileName;
		private SoundList(String fileName) {
			this.fileName = fileName;
		}
		public String getFileName() {
			return Res.SOUND + fileName;
		}
	}
	
	private boolean enabled = true;
	private float volume = 1f;
	private final LRUCache<SoundList, Sound> soundCache;
	
	public SoundManager() {
		soundCache = new LRUCache<SoundList, Sound>(2);
		soundCache.setEntryRemovedListener(this);
	}
	
	public void play(SoundList sound) {
		if(!enabled || sound == SoundList.NONE) return;
		
		Sound resource = soundCache.get(sound);
		// Cache miss
		if(resource == null) {
			FileHandle soundFile = Gdx.files.internal(sound.getFileName());
			resource = Gdx.audio.newSound(soundFile);
			soundCache.put(sound, resource);
		}
		
		// Play sound
		resource.play(volume);
	}
	
	public void setVolume(float volume) {
		if(volume < 0 || volume > 1f) {
			throw new IllegalArgumentException("Volume must be in the range [0,1]");
		}
		this.volume = volume;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void dispose() {
		for(Sound sound: soundCache.retrieveAll()) {
			sound.stop();
			sound.dispose();
		}
	}

	@Override
	public void notifyEntryRemoved(SoundList key, Sound value) {
		value.dispose();
	}
}
