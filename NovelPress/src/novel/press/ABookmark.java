package novel.press;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ABookmark extends Activity implements OnClickListener,OnFocusChangeListener {
	private LinearLayout allBase;
	private ScrollView lineBase;
	private TableLayout lineBase2;
	private TableRow[] line;
	private TextView[] lineTop;
	private TextView[] lineCom;
	private LinearLayout btnBase;
	private Button btnSave;
	private Button btnLoad;
	private int focusNo = -1;
	private long[] time;
	private boolean saveable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.saveable = getIntent().getExtras().getBoolean(NovelPress.ITTKEY_BOOKMARK);
		//時間を取得する
		this.time = new long[NovelPress._nooFile];
		for(int i=0; i<this.time.length; i++) {
			try {
				DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"savefile"+FUtil._formatNum((i+1),2)+".dat"));
				this.time[i] = dataIn.readLong();
				dataIn.close();
			} catch(Exception e) {}
		}
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//
		TableLayout.LayoutParams lineLp = new TableLayout.LayoutParams();
		lineLp.setMargins(1,1,0,1);
		//基盤レイアウト
		this.allBase = new LinearLayout(this);
		this.allBase.setOrientation(LinearLayout.VERTICAL);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
				NovelPress._getColor(NovelPress._sysColor_back),
				NovelPress._getColor(NovelPress._sysColor_cur2)}));
			//fileビュー
			this.lineBase = new ScrollView(this);
			this.lineBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
			//fileレイアウト
			this.lineBase2 = new TableLayout(this);
			this.lineBase2.setColumnStretchable(1,true);
				//file項目
				int lineH = ((int)new TextView(this).getTextSize())*3;
				this.line    = new TableRow[NovelPress._nooFile];
				this.lineTop = new TextView[NovelPress._nooFile];
				this.lineCom = new TextView[NovelPress._nooFile];
				for(int i=0; i<this.line.length; i++) {
					//番号
					this.lineTop[i] = new TextView(this);
					this.lineTop[i].setHeight(lineH);
					this.lineTop[i].setGravity(Gravity.CENTER_VERTICAL);
					this.lineTop[i].setPadding(10,0,0,0);
					this.lineTop[i].setText(NovelPress.STR_NUMIND_BOOKMARK+FUtil._formatNum(i+1,(""+NovelPress._nooFile).length()));
					this.lineTop[i].setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
					this.lineTop[i].setBackgroundDrawable(new ShapeDrawable(new RoundRectShape(new float[]{10,10,0,0,0,0,10,10},null,null)));
					//日時
					this.lineCom[i] = new TextView(this);
					this.lineCom[i].setHeight(lineH);
					this.lineCom[i].setGravity(Gravity.CENTER_VERTICAL);
					this.lineCom[i].setPadding(30,0,0,0);
					this.lineCom[i].setText(formatStr(this.time[i]));
					this.lineCom[i].setTextColor(NovelPress._getColor((this.time[i]>0)?NovelPress._sysColor_font:NovelPress._sysColor_font2));
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
			//saveloadボタンレイアウト
			this.btnBase = new LinearLayout(this);
				//saveボタン
				this.btnSave = new Button(this);
				this.btnSave.setText(NovelPress.STR_BTNLBL_BOOKMARK_SAVE);
				this.btnSave.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_cur));
				if(this.saveable) {
					this.btnSave.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
					this.btnSave.setOnClickListener(this);
				} else {
					this.btnSave.setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
				}
				//loadボタン
				this.btnLoad = new Button(this);
				this.btnLoad.setText(NovelPress.STR_BTNLBL_BOOKMARK_LOAD);
				this.btnLoad.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				this.btnLoad.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_cur));
				this.btnLoad.setOnClickListener(this);
		//紐付ける
		this.btnBase.addView(this.btnSave);
		this.btnBase.addView(this.btnLoad);
		for(int i=0; i<this.line.length; i++) {
			this.line[i].addView(this.lineTop[i]);
			this.line[i].addView(this.lineCom[i]);
			this.lineBase2.addView(this.line[i]);
		}
		this.lineBase.addView(this.lineBase2);
		this.allBase.addView(this.lineBase);
		this.allBase.addView(this.btnBase);
		setContentView(this.allBase);
		//サイズを設定する
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		setSize(display.getWidth(),display.getHeight());
	}
	private void setSize(int wW,int wH) {
		int btnH = ((int)this.btnSave.getTextSize())*3;
		this.allBase.setMinimumWidth(wW);
		this.allBase.setMinimumHeight(wH);
		this.lineBase.setLayoutParams(new LinearLayout.LayoutParams(wW,wH-btnH));
		this.btnSave.setWidth(wW/2);
		this.btnSave.setHeight(btnH);
		this.btnLoad.setWidth(wW/2);
		this.btnLoad.setHeight(btnH);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//サイズを再設定する
		setSize(this.allBase.getHeight(),this.allBase.getWidth());
	}
	@Override
	public void onFocusChange(View line,boolean hasFocus) {
		//fileViewを更新する
		int lineNo = line.getId();
		changeLineState(lineNo,hasFocus);
		//loadボタンを更新する
		if(hasFocus) {
			this.focusNo = lineNo;
			if(this.time[lineNo]>0) {
				this.btnLoad.setClickable(true);
				this.btnLoad.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
			} else {
				this.btnLoad.setClickable(false);
				this.btnLoad.setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
			}
		}
	}
	@Override
	public void onClick(View v) {
		if(this.focusNo<0) return;
		//saveボタン
		if(v==this.btnSave) {
			try {
				//保存する
				DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"savefile"+FUtil._formatNum((this.focusNo+1),2)+".dat"));
				NPFile file = NPFile._getFileRec();
				//time
				this.time[this.focusNo] = System.currentTimeMillis();
				dataOut.writeLong(this.time[this.focusNo]);
				//変数
				byte[] bs = FUtil._boolToByte(file.var);
				dataOut.write(bs);
				//データ
				dataOut.writeInt(file.index);
				dataOut.writeShort(file.txtareaX);
				dataOut.writeShort(file.txtareaY);
				dataOut.writeShort(file.txtareaW);
				dataOut.writeShort(file.txtareaH);
				dataOut.write(file.fontSpeed);
				dataOut.write(file.fontSize);
				dataOut.writeInt(file.fontColor);
				dataOut.write(file.fontSpaceW);
				dataOut.write(file.fontSpaceH);
				dataOut.writeShort(file.frameX);
				dataOut.writeShort(file.frameY);
				dataOut.writeShort(file.frameW);
				dataOut.writeShort(file.frameH);
				dataOut.writeInt(file.frameColor);
				dataOut.write(file.frameA);
				dataOut.writeBoolean(file.vib);
				dataOut.write(file.effect);
				//image
				if(file.bi==null) {
					dataOut.writeBoolean(false);
					dataOut.writeInt(file.biColor);
				}else {
					dataOut.writeBoolean(true);
					dataOut.write(file.bi.getImgNo());
				}
				for(int i=0; i<file.fi.length; i++) {
					dataOut.write(file.fi[i].getImgNo());
					dataOut.writeShort(file.fi[i].getX());
					dataOut.writeShort(file.fi[i].getY());
				}
				//mld
				for(int i=0; i<file.mld.length; i++) {
					dataOut.write(file.mld[i]);
				}
				dataOut.close();
				//ビューを更新する
				this.lineCom[this.focusNo].setText(formatStr(this.time[this.focusNo]));
				this.lineCom[this.focusNo].setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				this.btnLoad.setClickable(true);
				this.btnLoad.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
			} catch(Exception e) {
				NovelPress.showErrDialog("io_saveFile",""+this.focusNo,e);
			}
		}
		//loadボタン
		if(v==this.btnLoad) {
			try {
				DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"savefile"+FUtil._formatNum((this.focusNo+1),2)+".dat"));
				dataIn.skip(8);
				NPFile file = NPFile._getFileNow();
				file.init();
				//変数
				byte[] bs = new byte[128/8];
				dataIn.read(bs);
				file.var = FUtil._byteToBool(bs);
				//データ
				file.index      = dataIn.readInt();
				file.txtareaX   = dataIn.readShort();
				file.txtareaY   = dataIn.readShort();
				file.txtareaW   = dataIn.readShort();
				file.txtareaH   = dataIn.readShort();
				file.fontSpeed  = dataIn.readByte();
				file.fontSize   = dataIn.read();
				file.fontColor  = dataIn.readInt();
				file.fontSpaceW = dataIn.readByte();
				file.fontSpaceH = dataIn.readByte();
				file.frameX     = dataIn.readShort();
				file.frameY     = dataIn.readShort();
				file.frameW     = dataIn.readShort();
				file.frameH     = dataIn.readShort();
				file.frameColor = dataIn.readInt();
				file.frameA     = dataIn.readByte();
				file.vib        = dataIn.readBoolean();
				file.effect     = dataIn.read();
				//image
				if(dataIn.readBoolean()) {
					file.bi = new NPImage(true,dataIn.readByte());
				}else {
					file.biColor = dataIn.readInt();
				}
				for(int i=0; i<file.fi.length; i++) {
					file.fi[i].setImgNo(dataIn.readByte());
					file.fi[i].setX(dataIn.readShort());
					file.fi[i].setY(dataIn.readShort());
				}
				//mld
				file.mld = new int[NPCan.LIMIT_NOOMLD];
				for(int i=0; i<file.mld.length; i++) {
					file.mld[i] = dataIn.readByte();
				}
				//戻る
				Intent intent = new Intent();
				intent.putExtra(NovelPress.ITTKEY_BOOKMARK,true);//true:ロードした
				setResult(Activity.RESULT_OK,intent);
				finish();
			} catch(Exception e) {
				NovelPress.showErrDialog("io_loadFile",""+this.focusNo,e);
			}
		}
	}
	private void changeLineState(int lineNo,boolean hasFocus) {
		int color = (hasFocus)?NovelPress._getColor(NovelPress._sysColor_cur):NovelPress._getColor(NovelPress._sysColor_cur,100);
		((ShapeDrawable)this.lineTop[lineNo].getBackground()).getPaint().setColor(color);
		((ShapeDrawable)this.lineCom[lineNo].getBackground()).getPaint().setColor(color);
	}
	private String formatStr(long time) {
		if(time>0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(time));
			return
				FUtil._formatNum(cal.get(Calendar.MONTH)+1,2)    +"/"+
				FUtil._formatNum(cal.get(Calendar.DATE),2)       +" "+
				FUtil._formatNum(cal.get(Calendar.HOUR_OF_DAY),2)+":"+
				FUtil._formatNum(cal.get(Calendar.MINUTE),2);
		} else {
			return NovelPress.STR_OFFLBL_BOOKMARK;
		}
	}
}
