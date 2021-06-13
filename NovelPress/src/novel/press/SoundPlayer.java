package novel.press;

import java.util.*;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

////////////////////////////////////////////////////////////////////////////////////////////////////
// SoundPlayer
////////////////////////////////////////////////////////////////////////////////////////////////////
public class SoundPlayer {
	private static Vector<Sound> _mlds = new Vector<Sound>();
	private static int _volume;
	
	//操作
	public static boolean _play(int mldNo,boolean loop,int fadeinTime) {
		//同曲確認
		//両方ともloopだった場合のみ無視し、それ以外の場合は二重演奏
		for(int i=0; i<_mlds.size(); i++) {
			Sound m = (Sound)_mlds.elementAt(i);
			if(m.getFadeState()!=Sound.FADESTATE_OUT && m.getMldNo()==mldNo) {
				if(m.getLoop() && loop) {
					return false;
				}
			}
		}
		//生成
		Sound m = new Sound(mldNo,loop);
		if(m.play(fadeinTime)) {
			_mlds.addElement(m);
			return true;
		}else {
			return false;
		}
	}
	public static void _stop(int mldNo,int fadeTime) {
		for(int i=0; i<_mlds.size(); i++) {
			Sound m = (Sound)_mlds.elementAt(i);
			if(m.getMldNo()==mldNo) {
				m.stop(fadeTime);
			}
		}
	}
	public static void _allStop(int fadeTime) {
		for(int i=0; i<_mlds.size(); i++) {
			((Sound)_mlds.elementAt(i)).stop(fadeTime);
		}
	}
	//removeSound
	public static void _removeSound(Sound sound) {
		_mlds.removeElement(sound);
	}
	//音量
	public static int _getVolume() {
		return _volume;
	}
	public static void _setVolume(int volume) {
		_volume = FUtil._formatNum(volume,0,100);
		for(int i=0; i<_mlds.size(); i++) {
			((Sound)_mlds.elementAt(i)).setVolume(volume);
		}
	}
	//再生中トラック集合
	public static int[] _getList(int nooList) {
		int[] list = new int[nooList];
		
		//再生中トラック集合
		int listNo = 0;
		for(int i=0; i<_mlds.size(); i++) {
			Sound m = (Sound)(_mlds.elementAt(i));
			//loop演奏曲で、停止中でない曲のみ
			if(m.getLoop() && m.getFadeState()!=Sound.FADESTATE_OUT) {
				list[listNo++] = m.getMldNo();
			}
		}
		//残り
		for(int i=listNo; i<list.length; i++) {
			list[i] = -1;
		}
		return list;
	}
	
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Sound
////////////////////////////////////////////////////////////////////////////////////////////////////
class Sound extends Thread implements OnCompletionListener {
	private int mldNo;
	private boolean loop;
	private int fadeState;
	private int fadeTime;
	private long fadeAnc;
	private int volumeBuf;
	private MediaPlayer player;
	
	//コンストラクタ
	public Sound(int mldNo,boolean loop) {
		this.mldNo = mldNo;
		this.loop  = loop;
	}
	//アクセサ
	public boolean getLoop() {
		return this.loop;
	}
	public int getMldNo() {
		return this.mldNo;
	}
	public int getFadeState() {
		return this.fadeState;
	}
	//play
	public boolean play(int fadeTime) {
		String[] loc = new String[]{".ogg",".mp3"};
		
		//データ取得
		AssetFileDescriptor afd = null;
		for(int i=0; i<loc.length; i++) {
			try {
				afd = NovelPress._am.openFd("sound/"+FUtil._formatNum(this.mldNo,2)+loc[i]);
				break;
			} catch(Exception e) {}
		}
		if(afd==null) return false;
		//データをセットする
		this.player = new MediaPlayer();
		try {
			this.player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		} catch(Exception e) {
			this.player = null;
			return false;
		}
		//player準備
		this.player.setOnCompletionListener(this);
		if(fadeTime>0) {
			this.player.setVolume(0,0);
		}else {
			this.volumeBuf = SoundPlayer._getVolume();
			float volume = this.volumeBuf/100F;
			this.player.setVolume(volume,volume);
		}
		this.fadeTime  = fadeTime;
		this.fadeState = FADESTATE_IN;
		this.fadeAnc   = System.currentTimeMillis();
		try {
			this.player.prepare();
			this.player.seekTo(0);
			this.player.start();
		} catch(Exception e) {
			this.player = null;
			return false;
		}
		start();
		return true;
	}
	//stop
	public void stop(int fadeTime) {
		if(fadeTime>0) {
			this.volumeBuf = SoundPlayer._getVolume();
			float volume = this.volumeBuf/100F;
			this.player.setVolume(volume,volume);
		}else {
			this.player.setVolume(0,0);
		}
		this.fadeTime  = fadeTime;
		this.fadeState = FADESTATE_OUT;
		this.fadeAnc   = System.currentTimeMillis();
		setPriority(Thread.NORM_PRIORITY);
	}
	//setVolume
	public void setVolume(int volume) {
		if(this.fadeState==FADESTATE_WAIT) {
			float vol = volume/100F;
			this.player.setVolume(vol,vol);
		}
	}
	//run
	public void run() {
		while(true) {
			if(this.fadeState!=FADESTATE_WAIT) {
				//進捗の割り合いを計算
				int rate = 0;
				int fadeTime = this.fadeTime;
				if(fadeTime<=0) rate = 100;
				else {
					rate = (int)((System.currentTimeMillis()-this.fadeAnc)*100/fadeTime);
					if(rate>100) rate = 100;
				}
				//フェードイン
				if(this.fadeState==FADESTATE_IN) {
					int volume = SoundPlayer._getVolume()*rate/100;
					if(volume!=this.volumeBuf) {
						this.volumeBuf = volume;
						float vol = volume/100F;
						this.player.setVolume(vol,vol);
					}
					//フェード完了
					if(rate==100) {
						//待機状態に移行する
						this.fadeState = FADESTATE_WAIT;
						setPriority(Thread.MIN_PRIORITY);
					}
				}
				//フェードアウト
				if(this.fadeState==FADESTATE_OUT) {
					int volume = SoundPlayer._getVolume()*(100-rate)/100;
					if(volume!=this.volumeBuf) {
						this.volumeBuf = volume;
						float vol = volume/100F;
						this.player.setVolume(vol,vol);
					}
					//フェード完了
					if(rate==100) {
						//playerを廃棄する
						if(this.player!=null) {
							this.player.stop();
							this.player.setOnCompletionListener(null);
							this.player.release();
							this.player = null;
						}
						//再生中???から自身を削除する
						SoundPlayer._removeSound(this);
						break;
					}
				}
			}
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(this.loop) {
			this.player.seekTo(0);
			this.player.start();
		}else {
			stop(0);
		}
	}
	
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	public static final int
		FADESTATE_IN   = 0,
		FADESTATE_OUT  = 1,
		FADESTATE_WAIT = 2;



}
