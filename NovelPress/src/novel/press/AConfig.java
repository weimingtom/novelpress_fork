package novel.press;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AConfig extends Activity {
	private ScrollView allBase;
	private TableLayout allBase2;
	private TableRow txtSpeedBase;
	private TextView txtSpeedCom;
	private SeekBar txtSpeedBar;
	private TextView txtSpeedInd;
	private TableRow autoWaitBase;
	private TextView autoWaitCom;
	private SeekBar autoWaitBar;
	private TextView autoWaitInd;
	private TableRow volumeBase;
	private TextView volumeCom;
	private SeekBar volumeBar;
	private TextView volumeInd;
	private TableRow frameABase;
	private TextView frameACom;
	private SeekBar frameABar;
	private TextView frameAInd;
	private TableRow vibableBase;
	private TextView vibableCom;
	private NPToggleButton vibableBtn;
	private TableRow markCBase;
	private TextView markCCom;
	private NPToggleButton markCBtn;
	private TableRow markPBase;
	private TextView markPCom;
	private NPToggleButton markPBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//基盤レイアウト
		this.allBase = new ScrollView(this);
		this.allBase.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_back));
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
		this.allBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
		this.allBase2 = new TableLayout(this);
		this.allBase2.setColumnStretchable(1,true);
			//テキスト速度
			if(NovelPress._ciTxtSpeed) {
				this.txtSpeedBase = createRow();
					this.txtSpeedCom = createComView("文字の表示速度");
					this.txtSpeedBar = createBarView(10,_txtSpeed);
					this.txtSpeedInd = createIndView("速");
					//リスナ
					this.txtSpeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							_txtSpeed = seekBar.getProgress();
						}
					});
			}
			//オートウェイト
			if(NovelPress._ciAutoWait) {
				this.autoWaitBase = createRow();
					this.autoWaitCom = createComView("???????の????");
					this.autoWaitBar = createBarView(10,_autoWait);
					this.autoWaitInd = createIndView("長");
					//リスナ
					this.autoWaitBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							_autoWait = seekBar.getProgress();
						}
					});
			}
			//文字枠の透明度
			if(NovelPress._ciFrameA) {
				this.frameABase = createRow();
					this.frameACom = createComView("文字枠の透明度");
					this.frameABar = createBarView(10,_frameA);
					this.frameAInd = createIndView("透");
					//リスナ
					this.frameABar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							_frameA= seekBar.getProgress();
						}
					});
			}
			//音量
			if(NovelPress._ciVolume) {
				this.volumeBase = createRow();
					this.volumeCom = createComView("音量");
					this.volumeBar = createBarView(10,_volume);
					this.volumeInd = createIndView("大");
					//リスナ
					this.volumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
							_volume = seekBar.getProgress();
							SoundPlayer._setVolume(_volume*10);
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {}
					});
			}
			//バイブレーション
			if(NovelPress._ciVibable) {
				this.vibableBase = createRow();
					this.vibableCom = createComView("??????????");
					this.vibableBtn = createBtnView(_vibable);
					//リスナ
					this.vibableBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							_vibable = ((NPToggleButton)v).getChecked();
						}
					});
			}
			//クリック待ち
			if(NovelPress._ciMarkC) {
				this.markCBase = createRow();
					this.markCCom = createComView("????待ちの表示");
					this.markCBtn = createBtnView(_markC);
					//リスナ
					this.markCBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							_markC = ((NPToggleButton)v).getChecked();
						}
					});
			}
			//改頁待ち
			if(NovelPress._ciMarkP) {
				this.markPBase = createRow();
					this.markPCom = createComView("改頁待ちの表示");
					this.markPBtn = createBtnView(_markP);
					//リスナ
					this.markPBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							_markP = ((NPToggleButton)v).getChecked();
						}
					});
			}
		//紐付ける
		if(NovelPress._ciTxtSpeed) {
			this.txtSpeedBase.addView(this.txtSpeedCom);
			this.txtSpeedBase.addView(this.txtSpeedBar);
			this.txtSpeedBase.addView(this.txtSpeedInd);
			this.allBase2.addView(this.txtSpeedBase);
		}
		if(NovelPress._ciAutoWait) {
			this.autoWaitBase.addView(this.autoWaitCom);
			this.autoWaitBase.addView(this.autoWaitBar);
			this.autoWaitBase.addView(this.autoWaitInd);
			this.allBase2.addView(this.autoWaitBase);
		}
		if(NovelPress._ciFrameA) {
			this.frameABase.addView(this.frameACom);
			this.frameABase.addView(this.frameABar);
			this.frameABase.addView(this.frameAInd);
			this.allBase2.addView(this.frameABase);
		}
		if(NovelPress._ciVolume) {
			this.volumeBase.addView(this.volumeCom);
			this.volumeBase.addView(this.volumeBar);
			this.volumeBase.addView(this.volumeInd);
			this.allBase2.addView(this.volumeBase);
		}
		if(NovelPress._ciVibable) {
			this.vibableBase.addView(this.vibableCom);
			this.vibableBase.addView(this.vibableBtn);
			this.allBase2.addView(this.vibableBase);
		}
		if(NovelPress._ciMarkC) {
			this.markCBase.addView(this.markCCom);
			this.markCBase.addView(this.markCBtn);
			this.allBase2.addView(this.markCBase);
		}
		if(NovelPress._ciMarkP) {
			this.markPBase.addView(this.markPCom);
			this.markPBase.addView(this.markPBtn);
			this.allBase2.addView(this.markPBase);
		}
		this.allBase.addView(this.allBase2);
		setContentView(this.allBase);
	}
	private TableRow createRow() {
		TableRow row = new TableRow(this);
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams();
		lp.setMargins(0,5,0,5);
		row.setLayoutParams(lp);
		return row;
	}
	private TextView createComView(String str) {
		TextView com = new TextView(this);
		com.setText(str);
		com.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
		com.setPadding(5,5,5,5);
		return com;
	}
	private SeekBar createBarView(int max,int ini) {
		SeekBar bar = new SeekBar(this);
		bar.setMax(max);
		bar.setProgress(ini);
		//背景
		GradientDrawable grad = new GradientDrawable(Orientation.LEFT_RIGHT,new int[]{
			NovelPress._getColor(NovelPress._sysColor_font2),
			NovelPress._getColor(NovelPress._sysColor_font)});
		grad.setCornerRadius(5);
		bar.setProgressDrawable(grad);
		//つまみ(setSizeが使えるからGradientにした)
		GradientDrawable thumb = new GradientDrawable(Orientation.LEFT_RIGHT,new int[]{
			NovelPress._getColor(NovelPress._sysColor_cur),
			NovelPress._getColor(NovelPress._sysColor_cur)});
		thumb.setCornerRadius(3);
		int barH = (int)(new TextView(this).getTextSize());
		thumb.setSize(barH,barH*2);
		bar.setThumb(thumb);
		return bar;
	}
	private TextView createIndView(String str) {
		return createComView("["+str+"]");
	}
	private NPToggleButton createBtnView(boolean ini) {
		NPToggleButton btn = new NPToggleButton(this);
		TableRow.LayoutParams lp = new TableRow.LayoutParams();
		lp.span = 2;
		btn.setLayoutParams(lp);
		btn.setChecked(ini);
		return btn;
	}
	//////////////////////////////////////////////////
	// NPToggleButton
	//////////////////////////////////////////////////
	private class NPToggleButton extends Button {
		private boolean checked;
		private OnClickListener listener;
		
		public NPToggleButton(Context context) {
			super(context);
			setBackgroundDrawable(new ShapeDrawable(new RoundRectShape(new float[]{10,10,10,10,10,10,10,10},null,null)));
			setChecked(false);
			super.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					setChecked(!checked);
					if(listener!=null) listener.onClick(v);
				}
			});
		}
		public void setChecked(boolean checked) {
			this.checked = checked;
			if(this.checked) {
				setText("ON");
				setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				((ShapeDrawable)getBackground()).getPaint().setColor(NovelPress._getColor(NovelPress._sysColor_cur));
			} else {
				setText("OFF");
				setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
				((ShapeDrawable)getBackground()).getPaint().setColor(NovelPress._getColor(NovelPress._sysColor_cur,100));
			}
		}
		public boolean getChecked() {
			return this.checked;
		}
		@Override
		public void setOnClickListener(OnClickListener listener) {
			this.listener = listener;
		}
	}
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	protected static int
		_txtSpeed,
		_autoWait,
		_frameA,
		_volume;
	protected static boolean
		_vibable,
		_markC,
		_markP;

}
