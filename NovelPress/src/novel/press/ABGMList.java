package novel.press;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ABGMList extends Activity implements OnFocusChangeListener, OnClickListener {
	private LinearLayout allBase;
	private ScrollView lineBase;
	private TableLayout lineBase2;
	private TableRow[] line;
	private TextView[] lineTop;
	private TextView[] lineCom;
	private Button btn;
	private int focusNo = -1;
	private int playingNo = -1;
	private int[] bgmNo;
	private String[] bgmTitle;
	private int[] playingList;
	private boolean playerStateIsChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//現在再生中の曲を取得する
		this.playingList = SoundPlayer._getList(NPCan.LIMIT_NOOMLD);
		//ライン用パラメータ
		TableLayout.LayoutParams lineLp = new TableLayout.LayoutParams();
		lineLp.setMargins(1,1,0,1);
		//ラインの高さ
		int lineH = ((int)new TextView(this).getTextSize())*3;
		//基盤
		this.allBase = new LinearLayout(this);
		this.allBase.setOrientation(LinearLayout.VERTICAL);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
			//ライン基盤
			this.lineBase = new ScrollView(this);
			this.lineBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
			this.lineBase2 = new TableLayout(this);
			this.lineBase2.setColumnStretchable(1,true);
				//ライン
				Object[][] list = NPCan._getInstance().getBGMList();
				this.line     = new TableRow[list.length];
				this.lineTop  = new TextView[list.length];
				this.lineCom  = new TextView[list.length];
				this.bgmNo    = new int     [list.length];
				this.bgmTitle = new String  [list.length];
				for(int i=0; i<this.line.length; i++) {
					//リスト情報を取得する
					this.bgmNo[i] = ((Integer)list[i][0]).intValue();
					this.bgmTitle[i] = (String)list[i][1];
					//番号
					this.lineTop[i] = new TextView(this);
					this.lineTop[i].setHeight(lineH);
					this.lineTop[i].setGravity(Gravity.CENTER_VERTICAL);
					this.lineTop[i].setPadding(10,0,0,0);
					this.lineTop[i].setText(NovelPress.STR_NUMIND_BGMLIST+FUtil._formatNum(i+1,(""+list.length).length()));
					this.lineTop[i].setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
					this.lineTop[i].setBackgroundDrawable(new ShapeDrawable(new RoundRectShape(new float[]{10,10,0,0,0,0,10,10},null,null)));
					//曲名
					this.lineCom[i] = new TextView(this);
					this.lineCom[i].setHeight(lineH);
					this.lineCom[i].setGravity(Gravity.CENTER_VERTICAL);
					this.lineCom[i].setPadding(30,0,0,0);
					this.lineCom[i].setText((this.bgmTitle[i]==null)?NovelPress.STR_OFFLBL_BGMLIST:this.bgmTitle[i]);
					this.lineCom[i].setTextColor(NovelPress._getColor(this.bgmTitle[i]==null?NovelPress._sysColor_font2:NovelPress._sysColor_font));
					this.lineCom[i].setBackgroundDrawable(new ShapeDrawable(new RoundRectShape(new float[]{0,0,10,10,10,10,0,0},null,null)));
					//ライン
					this.line[i] = new TableRow(this);
					this.line[i].setId(i);
					this.line[i].setLayoutParams(lineLp);
					this.line[i].setOnClickListener(this);
					this.line[i].setFocusable(true);
					this.line[i].setFocusableInTouchMode(true);
					this.line[i].setOnFocusChangeListener(this);
					//フォーカス無し状態で初期化する
					changeLineState(i,false);
				}
			//再生ボタン
			this.btn = new Button(this);
			this.btn.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_cur));
			this.btn.setOnClickListener(this);
			changeBtnState(false,NovelPress.STR_BTNLBL_BGMLIST_PLAY);
		//紐付ける
		for(int i=0; i<this.line.length; i++) {
			this.line[i].addView(this.lineTop[i]);
			this.line[i].addView(this.lineCom[i]);
			this.lineBase2.addView(this.line[i]);
		}
		this.lineBase.addView(this.lineBase2);
		this.allBase.addView(this.lineBase);
		this.allBase.addView(this.btn);
		setContentView(this.allBase);
		//サイズを設定する
		updateViewSize();
	}
	@Override
	public void onClick(View v) {
		if(v!=this.btn) return;
		if(this.focusNo<0) return;
		//現在再生中の場合
		if(this.playingNo>=0) {
			//曲を停止する
			SoundPlayer._allStop(0);
			//再生中のリストをタップした場合
			if(this.focusNo==this.playingNo) {
				this.playingNo = -1;
				//再生ボタンを更新して終了する
				changeBtnState(true,NovelPress.STR_BTNLBL_BGMLIST_PLAY);
				return;
			}
		}
		//フラグOFFの場合
		String title = this.bgmTitle[this.focusNo];
		if(title==null) {
			//再生ボタンを更新する
			changeBtnState(false,NovelPress.STR_BTNLBL_BGMLIST_PLAY);
			this.playingNo = -1;
		}
		//フラグONの場合
		else {
			this.playingNo = this.focusNo;
			SoundPlayer._allStop(0);//シナリオ画面での曲を止めるため
			//演奏可能な場合は演奏する
			if(this.bgmNo[this.focusNo]>=0) {
				//再生する
				SoundPlayer._play(this.bgmNo[this.focusNo],true,0);
				//状態変化フラグをONにする
				this.playerStateIsChanged = true;
			}
			//再生ボタンを更新する
			changeBtnState(true,NovelPress.STR_BTNLBL_BGMLIST_PLAY+" "+title);
		}
	}
	@Override
	public void onFocusChange(View line, boolean hasFocus) {
		//ラインを更新する
		int lineNo = line.getId();
		changeLineState(lineNo,hasFocus);
		//フォーカスを得た場合
		if(hasFocus) {
			this.focusNo = lineNo;
			//曲が再生中でない場合は再生ボタンを更新する
			if(this.playingNo<0) {
				String title = this.bgmTitle[this.focusNo];
				changeBtnState(title!=null,NovelPress.STR_BTNLBL_BGMLIST_PLAY);
			}
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//サイズを再設定する
		updateViewSize();
	}
	@Override
	public void onPause() {
		//演奏した場合は演奏状態を復元する
		if(this.playerStateIsChanged) {
			SoundPlayer._allStop(0);
			if(this.playingList!=null) {
				for(int i=0; i<this.playingList.length; i++) {
					if(this.playingList[i]>=0) SoundPlayer._play(this.playingList[i],true,0);
				}
			}
		}
		super.onPause();
	}
	private void changeLineState(int lineNo,boolean hasFocus) {
		int color = (hasFocus)?NovelPress._getColor(NovelPress._sysColor_cur):NovelPress._getColor(NovelPress._sysColor_cur,100);
		((ShapeDrawable)this.lineTop[lineNo].getBackground()).getPaint().setColor(color);
		((ShapeDrawable)this.lineCom[lineNo].getBackground()).getPaint().setColor(color);
	}
	private void changeBtnState(boolean clickable,String str) {
		this.btn.setText(str);
		if(clickable) {
			this.btn.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
		} else {
			this.btn.setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
		}
	}
	private void updateViewSize() {
		//画面サイズを取得する
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int wW = display.getWidth();
		int wH = display.getHeight();
		//再生ボタンの高さを取得する
		int btnH = ((int)new TextView(this).getTextSize())*3;
		//サイズを変更する
		this.lineBase.setLayoutParams(new LinearLayout.LayoutParams(wW,wH-(AMenu._tabH+btnH)));
		this.btn.setHeight(btnH);
	}
}

