package novel.press;

import java.io.*;
import java.util.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.os.Vibrator;
import android.graphics.Matrix;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;

////////////////////////////////////////////////////////////////////////////////////////////////////
// NPCan
////////////////////////////////////////////////////////////////////////////////////////////////////
public class NPCan extends NPCanvasView {
	private boolean[]
		flagCGList,
		flagEDList,
		flagBGMList;
	//オートプレイ関連
	private boolean
		inAuto;
	//選択肢情報
	private Vector<int[]>
		selectLog = new Vector<int[]>();//int[2]{タグの位置,bool[]->Int}
	//outputタグ関連
	private String[]
		output;
	//シナリオ関連
	private char
		poseType;
	private int
		cX,
		cY;
	private String s;
	private StringBuffer
		sbNow,//現在描画中の文字列
		sbBac;//回想用の文字列
	private NPFile
		fileNow;//実行中ファイル
	private Bitmap imgNow;
	//その他
	private boolean inSkip;
	private boolean[] flagGlobal;//グローバルフラグ
	private int key;
	protected boolean finish;
	//ime用
	private Handler imeHandler = new Handler();
	private boolean imeFinFlg;
	private String imeStr;
	
	public NPCan(Context context) {
		super(context);
		_npcan = this;
		this.fileNow = NPFile._getFileNow();
	}
	//
	//根幹
	//
	//根幹
	public void run() {
		try{
		//アプリ準備
		//作品準備
		pre_init();
		//実行
		while(true) {
			scene_title();
			if(this.finish) break;
			scene_start();
			if(this.finish) break;
		}
		}catch(Exception e) {
			NovelPress.showErrDialog("不明","不明",e);
			//TODO ??????
			Process.killProcess(Process.myPid());
		}
		//アプリ終了処理
	}
	//作品準備
	private void pre_init() {
		//初回処理
		boolean firstFlg = new File(NovelPress._SDHome+"config.dat").exists();
		if(firstFlg==false) {
			AConfig._txtSpeed = NovelPress._iniTxtSpeed;
			AConfig._autoWait = NovelPress._iniAutoWait;
			AConfig._frameA   = NovelPress._iniFrameA;
			AConfig._volume   = NovelPress._iniVolume;
			AConfig._vibable  = NovelPress._iniVibable;
			AConfig._markC    = NovelPress._iniMarkC;
			AConfig._markP    = NovelPress._iniMarkP;
			io_saveConfig();
		}
		//テキスト
		s = FUtil._deleteStr(getTxt(),"\n\r\t");
		sbNow = new StringBuffer();
		sbBac = new StringBuffer();
		//グローバル変数
		byte[] data = new byte[128/8];
		try{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"gflag.dat"));
			dataIn.read(data);
			dataIn.close();
		} catch(Exception e) {}
		flagGlobal = FUtil._byteToBool(data);
		//CGリスト
		data = new byte[100/8+1];
		try{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"cglist.dat"));
			dataIn.read(data);
			dataIn.close();
		} catch(Exception e) {}
		flagCGList = FUtil._byteToBool(data);
		//EDリスト
		data = new byte[100/8+1];
		try{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"edlist.dat"));
			dataIn.read(data);
			dataIn.close();
		} catch(Exception e) {}
		flagEDList = FUtil._byteToBool(data);
		//BGMリスト
		data = new byte[100/8+1];
		try{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"bgmlist.dat"));
			dataIn.read(data);
			dataIn.close();
		} catch(Exception e) {}
		flagBGMList = FUtil._byteToBool(data);
		//config
		try {
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"config.dat"));
			AConfig._txtSpeed = dataIn.read();
			AConfig._autoWait = dataIn.read();
			AConfig._frameA   = dataIn.read();
			AConfig._volume   = dataIn.read();
			AConfig._vibable  = dataIn.readBoolean();
			AConfig._markC    = dataIn.readBoolean();
			AConfig._markP    = dataIn.readBoolean();
			dataIn.close();
		} catch(Exception e) {}
		SoundPlayer._setVolume(AConfig._volume*10);
		//選択肢情報
		if(NovelPress._selectLogable) {
			boolean updated = false;
			try {
				DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"selectlog.dat"));
				int nooLog = dataIn.readInt();
				for(int i=0; i<nooLog; i++) {
					int[] log = new int[]{dataIn.readInt(),dataIn.readShort()};
					//logが有効かどうかを判定（シナリオファイルに変更があった場合等のため）
					if(s!=null && s.substring(log[0],log[0]+7).startsWith("select,")) {
						selectLog.addElement(log);
					}else {
						updated = true;
					}
				}
				dataIn.close();
			} catch(Exception e) {}
			if(updated) io_saveSelectLog();
		}
		//output
		output = new String[LIMIT_NOOOUTPUT];
		try {
			DataInputStream dataIn = new DataInputStream(new FileInputStream(NovelPress._SDHome+"output.dat"));
			for(int i=0; i<output.length; i++) {
				int size = dataIn.read();
				if(size>0) {
					data = new byte[size];
					dataIn.read(data);
					output[i] = new String(data);
				}
			}
			dataIn.close();
		} catch(Exception e) {}
	}
	//タイトル
	private void scene_title() {
		//メニュー内容を変更する
		setMenu(NovelPress.MENUSET_TITLE);
		//タイトルbgm
		SoundPlayer._allStop(0);
		if(NovelPress._titleBGM) {
			SoundPlayer._play(0,true,NovelPress._titleBGMFadein);
		}
		//背景画像を読み込む
		Bitmap image = NovelPress._loadImage("imageB/00");
		if(image==null) setDrawSize(240,240);
		else            setDrawSize(image.getWidth(),image.getHeight());
		WIDTH  = this.drawW;
		HEIGHT = this.drawH;
		//メニュー文字列サイズ計算
		int titleMenuW = 0;
		int titleMenuH = 0;
		setFontSize(NovelPress._titleMenuSize);
		for(int i=0; i<NovelPress._titleMenu.length; i++) {
			if(NovelPress._titleMenu[i]!=null) {
				int cW = _stringWidth(NovelPress._titleMenu[i]);
				if(cW>titleMenuW) titleMenuW = cW;
			}
		}
		titleMenuW += NovelPress._titleMenuMgn*2;
		titleMenuH = (NovelPress._titleMenuSize+NovelPress._titleMenuMgn)*NovelPress._titleMenu.length+NovelPress._titleMenuMgn;
		//文字の色取得
		int fontColor2 = FUtil._getMidColor(NovelPress._sysColor_font,NovelPress._sysColor_cur);
		int shadeColor = FUtil._getMidColor(fontColor2,NovelPress._sysColor_cur);
		//フェードイン
		long timeAnc = System.currentTimeMillis();
		while(true) {
			int alpha = 255-255*((int)(System.currentTimeMillis()-timeAnc))/1000;
			if(alpha<=0) break;
			//描画
			lock();
			fillRect(NovelPress._getColor(NovelPress._sysColor_back));
			if(image!=null) {
				drawImage(image,0,0);
			}
			fillRect(NovelPress._getColor(0x000000,alpha));
			unlock();
		}
		//実行
		this.key = -1;
		while(true) {
			//キー
			int key = this.key;
			this.key = -1;
			//終了
			if(key==NovelPress.KEY_BACK || key==NovelPress.KEY_MENU_END) {
				Process.killProcess(Process.myPid());
			}
			//サイト
			if(key==NovelPress.KEY_MENU_SITE) {
				Intent intent = new Intent("android.intent.action.VIEW",Uri.parse(NovelPress._url));
				getContext().startActivity(intent);
			}
			//決定確認
			if(this.tchUp!=null) {
				int[] xy = transTouchXY(this.tchUp[0],this.tchUp[1]);
				boolean start = false;
				for(int i=0; i<NovelPress._titleMenu.length; i++) {
					int y = NovelPress._titleMenuY+NovelPress._titleMenuMgn+(NovelPress._titleMenuSize+NovelPress._titleMenuMgn)*i;
					if(xy[1]>=y && xy[1]<y+NovelPress._titleMenuSize) {
						if(i==0) {
							NPFile._getFileNow().init();
							start = true;
							break;
						}
						if(i==1) {
							if(NovelPress._nooFile>0) {
								Intent intent = new Intent(getContext(),ABookmark.class);
								intent.putExtra(NovelPress.ITTKEY_BOOKMARK,false);
								intent = ((NovelPress)getContext()).startMyActivity(intent);
								if(intent!=null && intent.getExtras().getBoolean(NovelPress.ITTKEY_BOOKMARK)) {
									start = true;
								}
							}
							break;
						}
						if(i==2) {
							((NovelPress)getContext()).startMyActivity(AMenu.class);
							io_saveConfig();
							break;
						}
						if(i==3) {
							((NovelPress)getContext()).startMyActivity(AStaff.class);
						}
					}
				}
				this.tchUp = null;
				if(start) break;
			}
			//描画
			setFontSize(NovelPress._titleMenuSize);
			lock();
			fillRect(NovelPress._getColor(NovelPress._sysColor_back));
			if(image!=null) drawImage(image,0,0);
			fillRect(NovelPress._titleMenuX,NovelPress._titleMenuY,titleMenuW,titleMenuH,NovelPress._getColor(NovelPress._sysColor_cur,150));
			for(int i=0; i<NovelPress._titleMenu.length; i++) {
				if(NovelPress._titleMenu[i]==null) continue;
				int sX = 0;
				if(NovelPress._titleMenuPos==0) sX = NovelPress._titleMenuX+NovelPress._titleMenuMgn;
				if(NovelPress._titleMenuPos==1) sX = NovelPress._titleMenuX+(titleMenuW-_stringWidth(NovelPress._titleMenu[i]))/2;
				if(NovelPress._titleMenuPos==2) sX = NovelPress._titleMenuX+titleMenuW-NovelPress._titleMenuMgn-_stringWidth(NovelPress._titleMenu[i]);
				int sY = NovelPress._titleMenuY+NovelPress._titleMenuMgn+(NovelPress._titleMenuSize+NovelPress._titleMenuMgn)*i;
				//タッチ確認
				boolean onTouch = false;
				for(int[] tch:this.tchNow) {
					int[] xy = transTouchXY(tch[1],tch[2]);
					if(xy[1]>=sY && xy[1]<sY+NovelPress._titleMenuSize) {
						onTouch = true;
						break;
					}
				}
				//描画
				drawString(NovelPress._titleMenu[i],sX+1,sY+1,NovelPress._getColor(shadeColor));
				if(onTouch) {
					drawString(NovelPress._titleMenu[i],sX-1,sY-1,NovelPress._getColor(NovelPress._sysColor_font));
				}else {
					drawString(NovelPress._titleMenu[i],sX,sY,NovelPress._getColor(fontColor2));
				}
			}
			unlock();
			FUtil._sleep(100);
		}
		//前処理
		setFile();
	}
	//シナリオ処理
	private void scene_start() {
		boolean inEscape = false;
		
		if(s==null) return;
		//メニュー内容を変更する
		setMenu(NovelPress.MENUSET_NONE);
		//実行
		inAuto = false;
		while(fileNow.index<s.length()) {
			//テキスト処理
			char c = s.charAt(fileNow.index);
			//System.out.println("@c:"+c);
			//escape
			if(c==CHAR_ESCAPE) {
				if(inEscape==false) {
					inEscape = true;
					sbNow.append(CHAR_ESCAPE);
					fileNow.index++;
					continue;
				}
			}
			//タグ処理
			if(c == '[' && inEscape==false) {
				int index_end = s.indexOf(']',fileNow.index);
				if(index_end<0) break;
				scene_start_exeTag(s.substring(fileNow.index+1,index_end));
				fileNow.index = s.indexOf(']',fileNow.index);
				if(fileNow.index<0) break;
			}
			//テキスト進行中の処理
			else {
				scene_start_exeC(c);
			}
			//次の解析対象へ
			fileNow.index++;
			inEscape = false;
			//posing系処理
			if(poseType!=0) {
				//[select]
				if(poseType=='s') {
					int timer = 0;
					String timeoutLbl = null;
					//トークンを取得する
					int index = s.lastIndexOf('[',fileNow.index-1)+1;
					int indexEnd = s.indexOf(']',index);
					String[] token = FUtil._splitString(s.substring(index,indexEnd),',');
					//肢を生成する
					Vector<String> vec = new Vector<String>();
					int i = 1;
					while(i<token.length) {
						//タイムアウト用の肢
						if(token[i].equals("timeout")) {
							timer = FUtil._parseNum(token[i+1]);
							if(timer<0) timer = 0;
							timeoutLbl = token[i+2];
							break;
						}
						//通常の肢
						if(token[i]==null) vec.addElement("");
						else               vec.addElement(token[i]);
						i += 2;
					}
					String[] selectStr = new String[vec.size()];
					vec.copyInto(selectStr);
					vec = null;
					//既読情報読み込み
					boolean[] bools = null;
					int flgs = 0;
					if(NovelPress._selectLogable) {
						for(i=0; i<selectLog.size(); i++) {
							int[] slog = (int[])selectLog.elementAt(i);
							if(slog[0]==index) {
								flgs = slog[1];
								bools = FUtil._byteToBool(
									new byte[]{
										(byte)(flgs>>8&0xff),
										(byte)(flgs   &0xff)});
								selectLog.removeElementAt(i);
								break;
							}
						}
					}
					//実行
					int result = scene_select(selectStr,bools,timer);
					//既読情報保存
					if(NovelPress._selectLogable && selectLog.size()<LIMIT_NOOSELECTLOG) {
						int[] newLog = new int[]{index,flgs};
						if(result>=0) {
							newLog[1] = flgs|(1<<(15-result));
						}
						selectLog.addElement(newLog);
						io_saveSelectLog();
					}
					//jumpする
					if(result>=0 || result==-2) {
						//目標ラベル名を取得する
						String lbl = (result>=0)?token[2*result+2]:timeoutLbl;
						//jumpする
						index = indexLabel(lbl);
						if(index>=0) {
							fileNow.index = s.indexOf(']',index)+1;
						}
					}
					inSkip = false;
				}
				//[c][p]
				if(poseType=='c' || poseType=='p') {
					if(inAuto) {
						if(AConfig._autoWait>0) {
							int waitTime = AConfig._autoWait*300;
							FUtil._sleep(waitTime);
						}
					}else {
						scene_posing();
						inSkip = false;
					}
				}
				//[p][P]
				if(poseType=='p' || poseType=='P') {
					copyToFileRec();
					//回想文字列に、描画中の文字列からタグを抜いたものを追加する
					int index = 0;
					while(index<sbNow.length()) {
						char cc = sbNow.charAt(index);
						if(cc==CHAR_TAG) {
							index++;
							while(sbNow.charAt(index++)!=CHAR_TAG);
						} else {
							sbBac.append(cc);
							index++;
						}
					}
					//改行3つ（改頁を意味する）を追加する
					sbBac.append(""+CHAR_LF+CHAR_LF+CHAR_LF);
					//回想文字数制限
					if(sbBac.length()>LIMIT_RESTORE) {
						index = sbBac.length()-LIMIT_RESTORE;
						while(index<sbBac.length()) {
							if(sbBac.charAt(index++)==CHAR_LF) {
								while(index<sbBac.length()) {
									if(sbBac.charAt(index++)!=CHAR_LF) {
										index--;
										break;
									}
								}
								break;
							}
						}
						sbBac.delete(0,index);
					}
					//描画中の文字列を消去する
					sbNow.delete(0,sbNow.length());
					cX = fileNow.txtareaX;
					cY = fileNow.txtareaY;
					if(NPFile._getFileBuf()!=null) {
						NPFile._deleteFileBuf();
						this.imgNow = null;
					}
				}
				poseType = 0;
			}
		}
		//終了処理
		setVibration(false);
	}
	//文字処理
	private void scene_start_exeC(char c) {
		sbNow.append(c);
		//オート時
		if(inAuto) {
			//描画
			lock();
			draw();
			unlock();
			//sleep(オート解除判定のため、FUtil._sleep()は使えない)
			int waitTime = 15*(10-(fileNow.fontSpeed>=0?fileNow.fontSpeed:AConfig._txtSpeed));
			long anchor = System.currentTimeMillis();
			while(true) {
				if(this.tchUp!=null) {
					this.tchUp = null;
					inAuto = false;
					break;
				}
				if(System.currentTimeMillis()>anchor+waitTime) break;
			}
		}
		//通常時
		else {
			//sleep時間計算
			int waitTime = 0;
			if(inSkip==false) {
				if(this.tchUp==null) {
					waitTime = 15*(10-(fileNow.fontSpeed>=0?fileNow.fontSpeed:AConfig._txtSpeed));
					if(this.tchNow.size()>0) {
						waitTime /= 3;
						if(waitTime==0) waitTime = 1;
					}
				}
			}
			//描画
			if(waitTime>0) {
				lock();
				draw();
				unlock();
				FUtil._sleep(waitTime);
			}else {
				inSkip = true;
			}
			//オート開始
			//if(ks[Display.KEY_POUND]==1) {
			//	inSkip = false;
			//	inAuto = true;
			//}
		}
		//ソフトラベル描画
		//setMenu(NovelPress.MENUSET_SCENARIO);
	}
	//タグ処理
	private void scene_start_exeTag(String str) {
		String[] token = FUtil._splitString(str,',');
		String tag = token[0];
		
		//System.out.println("@tag:"+str);
		try{
			if(false);
			//bgm
			else if(tag.equals("bgm")) {
				int bgmNo = FUtil._parseNum(token[1]);
				flagBGMList[bgmNo-1] = true;
				io_saveBGM();
			}
			//bgmoff
			else if(tag.equals("bgmoff")) {
				int mldNo    = FUtil._parseNum(token[1]);
				int fadeTime = 0;
				if(token.length>=3) fadeTime = FUtil._parseNum(token[2]);
				if(mldNo<0) {
					SoundPlayer._allStop(fadeTime);
				}else {
					SoundPlayer._stop(mldNo,fadeTime);
				}
			}
			//bgmon
			else if(tag.equals("bgmon")) {
				int mldNo    = FUtil._parseNum(token[1]);
				int fadeTime = 0;
				if(token.length>=3) fadeTime = FUtil._parseNum(token[2]);
				SoundPlayer._play(mldNo,true,fadeTime);
			}
			else if(tag.equals("BGMON")) {
				int mldNo    = FUtil._parseNum(token[1]);
				int fadeTime = 0;
				if(token.length>=3) fadeTime = FUtil._parseNum(token[2]);
				SoundPlayer._play(mldNo,false,fadeTime);
			}
			//bi
			else if(tag.equals("bi")) {
				if(token[1].equals("m")) {
					int r = FUtil._formatNum(FUtil._parseNum(token[2]),0,255);
					int g = FUtil._formatNum(FUtil._parseNum(token[3]),0,255);
					int b = FUtil._formatNum(FUtil._parseNum(token[4]),0,255);
					fileNow.setImageB(false,FUtil._getColor(r,g,b));
				}else {
					fileNow.setImageB(true,FUtil._parseNum(token[1]));
				}
			}
			//boxcolor
			else if(tag.equals("boxcolor")) {
				int r = (fileNow.frameColor>>16)&0xff;
				int g = (fileNow.frameColor>> 8)&0xff;
				int b = (fileNow.frameColor    )&0xff;
				if(token[1]!=null) r = FUtil._formatNum(FUtil._parseNum(token[1]),0,255);
				if(token[2]!=null) g = FUtil._formatNum(FUtil._parseNum(token[2]),0,255);
				if(token[3]!=null) b = FUtil._formatNum(FUtil._parseNum(token[3]),0,255);
				fileNow.frameColor = FUtil._getColor(r,g,b);
				if(token.length==5) {
					if(token[4]!=null) fileNow.frameA = FUtil._formatNum(FUtil._parseNum(token[4]),-1,10);
				}
			}
			//boxpos
			else if(tag.equals("boxpos")) {
				if(token[1]!=null) fileNow.frameX = FUtil._formatNum(FUtil._parseNum(token[1]),Short.MIN_VALUE,Short.MAX_VALUE);
				if(token[2]!=null) fileNow.frameY = FUtil._formatNum(FUtil._parseNum(token[2]),Short.MIN_VALUE,Short.MAX_VALUE);
				if(token[3]!=null) fileNow.frameW = FUtil._formatNum(FUtil._parseNum(token[3]),0,Short.MAX_VALUE);
				if(token[4]!=null) fileNow.frameH = FUtil._formatNum(FUtil._parseNum(token[4]),0,Short.MAX_VALUE);
			}
			//c
			else if(tag.equals("c")) {
				poseType = 'c';
			}
			//cg
			else if(tag.equals("cg")) {
				int cgNo = FUtil._parseNum(token[1]);
				flagCGList[cgNo-1] = true;
				io_saveCG();
			}
			//effect
			else if(tag.equals("effect")) {
				if(token[1].equals("none" )) fileNow.setEffect(NPFile.EFFECT_NONE);
				if(token[1].equals("mono" )) fileNow.setEffect(NPFile.EFFECT_MONO);
				if(token[1].equals("sepia")) fileNow.setEffect(NPFile.EFFECT_SEPIA);
				if(token[1].equals("crev" )) fileNow.setEffect(NPFile.EFFECT_CREV);
			}
			//fi
			else if(tag.equals("fi")) {
				int imgNo = FUtil._parseNum(token[1]);
				int pos   = FUtil._parseNum(token[2]);
				fileNow.setImageF(pos-1,imgNo);
			}
			//fioff
			else if(tag.equals("fioff")) {
				if(token.length==1) {
					fileNow.setImageF(-1,-1);
				}else {
					int pos = FUtil._parseNum(token[1]);
					fileNow.setImageF(pos-1,-1);
				}
			}
			//fixy
			else if(tag.equals("fixy")) {
				int pos = FUtil._parseNum(token[1]);
				int x   = FUtil._parseNum(token[2]);
				int y   = FUtil._parseNum(token[3]);
				fileNow.setImageFXY(pos-1,x,y);
			}
			//flagoff
			else if(tag.equals("flagoff")) {
				int flagNo = FUtil._parseNum(token[1]);
				if(flagNo>=129) {
					flagGlobal[flagNo-129] = false;
					io_saveGlobal();
				}else {
					fileNow.var[flagNo-1] = false;
				}
			}
			//flagon
			else if(tag.equals("flagon")) {
				int flagNo = FUtil._parseNum(token[1]);
				if(flagNo>=129) {
					flagGlobal[flagNo-129] = true;
					io_saveGlobal();
				}else {
					fileNow.var[flagNo-1] = true;
				}
			}
			//flash
			else if(tag.equals("flash")) {
				int time = FUtil._parseNum(token[1]);
				int color = 0xffffff;
				if(token.length==5) {
					color = FUtil._getColor(
						FUtil._formatNum(FUtil._parseNum(token[2]),0,255),
						FUtil._formatNum(FUtil._parseNum(token[3]),0,255),
						FUtil._formatNum(FUtil._parseNum(token[4]),0,255));
				}
				//実行
				//unlock();
				lock();
				fillRect(NovelPress._getColor(color,255));
				unlock();
				long anc = System.currentTimeMillis();
				while(System.currentTimeMillis()<anc+time);
			}
			//if
			else if(tag.equals("if")) {
				//判定
				boolean flag = true;
				for(int i=1; i<token.length-1; i++) {
					int flagNo = FUtil._parseNum(token[i]);
					if(flagNo>=129) {
						flag = flagGlobal[flagNo-129];
					}else {
						flag = fileNow.var[flagNo-1];
					}
					if(flag==false) break;
				}
				//jump
				if(flag) {
					int index = indexLabel(token[token.length-1]);
					if(index>=0) fileNow.index = index;
				}
			}
			//input
			else if(tag.equals("input")) {
				int inNo = FUtil._parseNum(token[1])-1;
				if(inNo>=0 || inNo<output.length) {
					//直接入力
					if(token.length==3) {
						String in = FUtil._deleteStr(token[2],"\r\n\t");
						if(in==null) in = "";
						if(in.length()>10) {
							in = in.substring(0,10);
						}
						output[inNo] = in;
						io_saveOutput();
					}
					//プレイヤー入力
					else {
						String in = ime(output[inNo]);
						if(in!=output[inNo]) {
							in = FUtil._deleteStr(in,"\r\n\t");
							if(in==null) in = "";
							if(in.length()>10) {
								in = in.substring(0,10);
							}
							output[inNo] = in;
							io_saveOutput();
						}
					}
				}
			}
			//jump
			else if(tag.equals("jump")) {
				int index = indexLabel(token[1]);
				if(index>=0) {
					fileNow.index = index;
				}
			}
			//l
			else if(tag.equals("l")) {
				sbNow.append(CHAR_LF);
			}
			//lock
			else if(tag.equals("lock")) {
				if(NPFile._getFileBuf()==null) {
					copyToFileBuf();
					this.imgNow = null;
				}
			}
			//unlock
			else if(tag.equals("unlock")) {
				NPFile fileBuf = NPFile._getFileBuf();
				if(fileBuf==null) return;
				//時間
				int time = 0;
				if(token.length>1) {
					time = FUtil._parseNum(token[1]);
				}
				//オプション
				char dir = 'l';
				boolean drawTxt = false;
				if(token.length>2) {
					if(token[2].indexOf('u')>=0) dir = 'u';
					if(token[2].indexOf('d')>=0) dir = 'd';
					if(token[2].indexOf('r')>=0) dir = 'r';
					if(token[2].indexOf('l')>=0) dir = 'l';
					if(token[2].indexOf('t')>=0) drawTxt = true;
				}
				//瞬間切り替え
				if(token.length<3 || time==0) ;
				else {
					Bitmap imgN = getImage(fileNow);
					Bitmap imgB = getImage(fileBuf);
					//実行
					long timeAnc = System.currentTimeMillis();
					//フェードイン
					if(token[2].indexOf('f')>=0) {
						this.p.setAlpha(255);
						Paint p = new Paint(this.p);
						while(true) {
							long timeN = System.currentTimeMillis();
							if(timeN>timeAnc+time) break;
							int alphaB = (int)(255*(timeN-timeAnc)/time);
							p.setAlpha(alphaB);
							lock();
							this.can.drawBitmap(imgB,this.drawX,this.drawY,this.p);
							this.can.drawBitmap(imgN,this.drawX,this.drawY,p);
							if(drawTxt) {
								int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
								fillRect(fileNow.frameX,fileNow.frameY,fileNow.frameW,fileNow.frameH,NovelPress._getColor(fileNow.frameColor,255*(10-alpha)/10));
								drawTxt();
							}
							unlock();
						}
					}
					//ブラインド
					else if(token[2].indexOf('b')>=0) {
						int noo = 10;
						int thick;//最大の太さ
						if(dir=='u' || dir=='d') thick = imgN.getWidth()/noo;
						else                     thick = imgN.getHeight()/noo;
						this.p.setAlpha(255);
						while(true) {
							long timeN = System.currentTimeMillis();
							if(timeN>timeAnc+time) break;
							int thickN = (int)(thick*(timeN-timeAnc)/time);//現在の太さ
							lock();
							this.can.drawBitmap(imgB,this.drawX,this.drawY,this.p);
							for(int i=0; i<noo; i++) {
								if(dir=='u') {
									int s = thick*(i+1)-thickN;
									drawImage(imgN,this.drawX,this.drawY+s,0,s,imgN.getWidth(),thickN);
								}
								if(dir=='d') {
									int s = thick*i;
									drawImage(imgN,this.drawX,this.drawY+s,0,s,imgN.getWidth(),thickN);
								}
								if(dir=='r') {
									int s = thick*i;
									drawImage(imgN,this.drawX+s,this.drawY,s,0,thickN,imgN.getHeight());
								}
								if(dir=='l') {
									int s = thick*(i+1)-thickN;
									drawImage(imgN,this.drawX+s,this.drawY,s,0,thickN,imgN.getHeight());
								}
							}
							if(drawTxt) {
								int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
								fillRect(fileNow.frameX,fileNow.frameY,fileNow.frameW,fileNow.frameH,NovelPress._getColor(fileNow.frameColor,255*(10-alpha)/10));
								drawTxt();
							}
							unlock();
						}
					}
					//スクロール
					else if(token[2].indexOf('s')>=0) {
						this.p.setAlpha(255);
						while(true) {
							long timeN = System.currentTimeMillis();
							if(timeN>timeAnc+time) break;
							int bX = 0;
							int bY = 0;
							int nX = 0;
							int nY = 0;
							if(dir=='u' || dir=='d') {
								int border = (int)(imgN.getHeight()*(timeN-timeAnc)/time);
								if(dir=='u') {
									bY = -border;
									nY = imgN.getHeight()-border;
								}else {
									bY = border;
									nY = border-imgN.getHeight();
								}
							}else {
								int border = (int)(imgN.getWidth()*(timeN-timeAnc)/time);
								if(dir=='r') {
									bX = border;
									nX = border-imgN.getWidth();
								}else {
									bX = -border;
									nX = imgN.getWidth()-border;
								}
							}
							lock();
							this.can.drawBitmap(imgB,this.drawX+bX,this.drawY+bY,this.p);
							this.can.drawBitmap(imgN,this.drawX+nX,this.drawY+nY,this.p);
							if(drawTxt) {
								int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
								fillRect(fileNow.frameX,fileNow.frameY,fileNow.frameW,fileNow.frameH,NovelPress._getColor(fileNow.frameColor,255*(10-alpha)/10));
								drawTxt();
							}
							unlock();
						}
					}
					//クアドラングル
					else if(token[2].indexOf('q')>=0) {
						int split = 20;//縦横20分割
						//順番リスト生成
						int[] list = new int[split*split];
						Vector<Integer> vec = new Vector<Integer>();
						for(int i=0; i<list.length; i++) vec.addElement(new Integer(i));
						for(int i=0; i<list.length; i++) {
							Integer no = (Integer)(vec.elementAt(FUtil._randomInt(vec.size())));
							vec.removeElement(no);
							list[i] = no.intValue();
						}
						//描画
						int cellW = this.drawW/split;
						int cellH = this.drawH/split;
						timeAnc = System.currentTimeMillis();
						this.p.setAlpha(255);
						while(true) {
							long timeN = System.currentTimeMillis();
							if(timeN>timeAnc+time) break;
							int endNo = (int)(list.length*(timeN-timeAnc)/time);
							lock();
							this.can.drawBitmap(imgB,this.drawX,this.drawY,this.p);
							for(int i=0; i<endNo; i++) {
								int cellNo = list[i];
								int cellX = cellW*(cellNo%split);
								int cellY = cellH*(cellNo/split);
								drawImage(imgN,this.drawX+cellX,this.drawY+cellY,cellX,cellY,cellW,cellH);
							}
							if(drawTxt) {
								int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
								fillRect(fileNow.frameX,fileNow.frameY,fileNow.frameW,fileNow.frameH,NovelPress._getColor(fileNow.frameColor,255*(10-alpha)/10));
								drawTxt();
							}
							unlock();
						}
					}
					//ページめくり
					else if(token[2].indexOf('p')>=0) {
						//反転画像を生成する
						Matrix m = new Matrix();
						if(dir=='u' || dir=='d') m.setScale(1F,-1F);
						else                     m.setScale(-1F,1F);
						Bitmap imgB2 = Bitmap.createBitmap(imgB,0,0,imgB.getWidth(),imgB.getHeight(),m,true);
						//実行
						this.p.setAlpha(255);
						while(true) {
							long timeN = System.currentTimeMillis();
							if(timeN>timeAnc+time) break;
							lock();
							this.can.drawBitmap(imgN,this.drawX,this.drawY,this.p);
							if(dir=='u') {
								int h = (int)(this.drawH*(timeN-timeAnc)/time);
								if(h<imgB.getHeight()/2) drawImage(imgB,this.drawX,this.drawY,0,0,imgB.getWidth(),imgB.getHeight()-h*2);
								drawImage(imgB2,this.drawX,this.drawY+this.drawH-h*2,0,0,imgB2.getWidth(),h);
							}
							if(dir=='d') {
								int h = (int)(this.drawH*(timeN-timeAnc)/time);
								if(h<imgB.getHeight()/2) drawImage(imgB,this.drawX,this.drawY+h*2,0,h*2,imgB.getWidth(),imgB.getHeight()-h*2);
								drawImage(imgB2,this.drawX,this.drawY+h,0,imgB2.getHeight()-h,imgB2.getWidth(),h);
							}
							if(dir=='r') {
								int w = (int)(this.drawW*(timeN-timeAnc)/time);
								if(w<imgB.getWidth()/2) drawImage(imgB,this.drawX+w*2,this.drawY,w*2,0,imgB.getWidth()-w*2,imgB.getHeight());
								drawImage(imgB2,this.drawX+w,this.drawY,imgB2.getWidth()-w,0,w,imgB2.getHeight());
							}
							if(dir=='l') {
								int w = (int)(this.drawW*(timeN-timeAnc)/time);
								if(w<imgB.getWidth()/2) drawImage(imgB,this.drawX,this.drawY,0,0,imgB.getWidth()-w*2,imgB.getHeight());
								drawImage(imgB2,this.drawX+this.drawW-w*2,this.drawY,0,0,w,imgB2.getHeight());
							}
							if(drawTxt) {
								int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
								fillRect(fileNow.frameX,fileNow.frameY,fileNow.frameW,fileNow.frameH,NovelPress._getColor(fileNow.frameColor,255*(10-alpha)/10));
								drawTxt();
							}
							unlock();
						}
					}
				}
				NPFile._deleteFileBuf();
				this.imgNow = null;
			}
			//output
			else if(tag.equals("output")) {
				int outNo = FUtil._parseNum(token[1])-1;
				if(outNo>=0 && outNo<output.length && output[outNo]!=null) {
					for(int i=0; i<output[outNo].length(); i++) {
						scene_start_exeC(output[outNo].charAt(i));
					}
				}
			}
			//p
			else if(tag.equals("p") || tag.equals("P")) {
				poseType = tag.charAt(0);
			}
			//saveon off
			else if(tag.equals("saveoff")) {
				this.fileNow.savable = false;
			}
			else if(tag.equals("saveon")) {
				this.fileNow.savable = true;
			}
			//select
			else if(tag.equals("select")) {
				poseType = 's';
			}
			//shake
			else if(tag.equals("shake")) {
				int lv   = FUtil._parseNum(token[1])*this.drawRate/_RATE;
				int time = FUtil._parseNum(token[2]);
				//揺らす
				long l = System.currentTimeMillis();
				while(System.currentTimeMillis()<l+time) {
					lock();
					this.can.translate(-lv/2+FUtil._randomInt(lv),-lv/2+FUtil._randomInt(lv));
					draw();
					unlock();
				}
			}
			//txtcolor
			else if(tag.equals("txtcolor")) {
				int r = (fileNow.fontColor>>16)&0xff;
				int g = (fileNow.fontColor>> 8)&0xff;
				int b = (fileNow.fontColor    )&0xff;
				if(token[1]!=null) r = FUtil._formatNum(FUtil._parseNum(token[1]),0,255);
				if(token[2]!=null) g = FUtil._formatNum(FUtil._parseNum(token[2]),0,255);
				if(token[3]!=null) b = FUtil._formatNum(FUtil._parseNum(token[3]),0,255);
				fileNow.fontColor = FUtil._getColor(r,g,b);
				sbNow.append(
					""+CHAR_TAG+CHAR_TAG_TXTCOLOR+
					(char)r+
					(char)g+
					(char)b+
					CHAR_TAG);
			}
			//txtpos
			else if(tag.equals("txtpos")) {
				if(token[1]!=null) fileNow.txtareaX = FUtil._formatNum(FUtil._parseNum(token[1]),Short.MIN_VALUE,Short.MAX_VALUE);
				if(token[2]!=null) fileNow.txtareaY = FUtil._formatNum(FUtil._parseNum(token[2]),Short.MIN_VALUE,Short.MAX_VALUE);
				if(token[3]!=null) fileNow.txtareaW = FUtil._formatNum(FUtil._parseNum(token[3]),0,Short.MAX_VALUE);
				if(token[4]!=null) fileNow.txtareaH = FUtil._formatNum(FUtil._parseNum(token[4]),0,Short.MAX_VALUE);
			}
			//txtsize
			else if(tag.equals("txtsize")) {
				int size = FUtil._formatNum(FUtil._parseNum(token[1]),2,Byte.MAX_VALUE-1);
				if(size%2!=0) size--;
				fileNow.fontSize = size;
				setFontSize(fileNow.fontSize);
				sbNow.append(
					""+CHAR_TAG+CHAR_TAG_TXTSIZE+
					(char)(fileNow.fontSize&0xFF)+
					CHAR_TAG);
			}
			//txtspace
			else if(tag.equals("txtspace")) {
				if(token[1]!=null) fileNow.fontSpaceW = FUtil._formatNum(FUtil._parseNum(token[1]),-128,127);
				if(token[2]!=null) fileNow.fontSpaceH = FUtil._formatNum(FUtil._parseNum(token[2]),-128,127);
				sbNow.append(
					""+CHAR_TAG+CHAR_TAG_TXTSPACE+
					(char)(fileNow.fontSpaceW&0xff)+
					(char)(fileNow.fontSpaceH&0xff)+
					CHAR_TAG);
			}
			//txtspeed
			else if(tag.equals("txtspeed")) {
				this.inSkip = false;
				fileNow.fontSpeed = FUtil._formatNum(FUtil._parseNum(token[1]),-1,10);
			}
			//url
			else if(tag.equals("url")) {
				String url = token[1];
				if(url!=null && url.length()>0) {
					Intent intent = new Intent("android.intent.action.VIEW",Uri.parse(url));
					getContext().startActivity(intent);
				}
			}
			//viboff
			else if(tag.equals("viboff")) {
				fileNow.vib = false;
				setVibration(fileNow.vib);
			}
			//vibon
			else if(tag.equals("vibon")) {
				fileNow.vib = true;
				setVibration(fileNow.vib);
			}
			//wait
			else if(tag.equals("wait") || tag.equals("WAIT")) {
				int time = FUtil._parseNum(token[1]);
				lock();
				draw();
				unlock();
				this.tchUp = null;
				long l = System.currentTimeMillis();
				while(System.currentTimeMillis()<l+time) {
					if(tag.equals("wait")) {
						if(this.tchUp!=null) break;
					}
				}
			}
			//z
			else if(tag.equals("z") || tag.equals("Z")) {
				if(token.length==3) {
					int edNo = FUtil._parseNum(token[1]);
					flagEDList[edNo-1] = true;
					io_saveED();
				}
				if(tag.equals("z")) fileNow.index = s.length();
			}
		}catch(Exception e) {
			NovelPress.showErrDialog("scene_start_exeTag","["+str+"]",e);
		}
	}
	//posing系
	private void scene_posing() {
		int markAlpha = 0;

		//メニュー内容を変更する
		setMenu(NovelPress.MENUSET_POSING);
		//実行
		this.tchUp = null;
		this.key = -1;
		while(true) {
			//キー
			int key = this.key;
			this.key = -1;
			//進む
			if(this.tchUp!=null) {
				//メニュー内容を変更する
				setMenu(NovelPress.MENUSET_NONE);
				//進む
				this.tchUp = null;
				return;
			}
			//メニュー
			else if(key==NovelPress.KEY_MENU_MENU) {
				setVibration(false);
				((NovelPress)getContext()).startMyActivity(AMenu.class);
				io_saveConfig();
				if(this.fileNow.vib) setVibration(true);
			}
			//栞
			else if(key==NovelPress.KEY_MENU_BOOKMARK) {
				if(NovelPress._nooFile>0) {
					Intent intent = new Intent(getContext(),ABookmark.class);
					intent.putExtra(NovelPress.ITTKEY_BOOKMARK,this.fileNow.savable);
					//実行
					setVibration(false);
					intent = ((NovelPress)getContext()).startMyActivity(intent);
					//ロードした場合
					if(intent!=null && intent.getExtras().getBoolean(NovelPress.ITTKEY_BOOKMARK)) {
						setFile();
						break;
					} else {
						if(this.fileNow.vib) setVibration(true);
					}
				}
			}
			//自動
			else if(key==NovelPress.KEY_MENU_AUTOREAD) {
				//メニュー内容を変更する
				setMenu(NovelPress.MENUSET_NONE);
				//進む
				this.inAuto = true;
				return;
			}
			//タイトルへ戻る
			else if(key==NovelPress.KEY_MENU_TITLE) {
				fileNow.index = s.length();
				return;
			}
			//テキストを消す
			else if(key==NovelPress.KEY_MENU_TXTOFF) {
				//メニューを消す
				setMenu(NovelPress.MENUSET_NONE);
				setVibration(false);
				//背景画像を生成する
				NPFile fileBuf = NPFile._getFileBuf();
				Bitmap img = (fileBuf==null)?getImage(this.fileNow):getImage(fileBuf);
				//タッチされるまで待機する
				this.p.setAlpha(255);
				this.tchUp = null;
				while(this.tchUp==null) {
					lock();
					this.can.drawBitmap(img,drawX,drawY,this.p);
					unlock();
				}
				//メニューを元に戻す
				setMenu(NovelPress.MENUSET_POSING);
				if(this.fileNow.vib) setVibration(true);
				this.tchUp = null;
			}
			//回想
			else if(key==NovelPress.KEY_MENU_READBACK) {
				if(this.sbBac.length()>0) {
					Intent itt = new Intent(getContext(),AReadback.class);
					itt.putExtra(NovelPress.ITTKEY_READBACK,this.sbBac.toString());
					setVibration(false);
					((NovelPress)getContext()).startMyActivity(itt);
					if(this.fileNow.vib) setVibration(true);
				}
			}
			/*int[] ks = getKeyState();
			}else if(ks[Display.KEY_POUND]==1) {
				//オート開始
				if(autable) {
					inAuto = true;
					return;
				}
			}*/
			//描画
			lock();
			draw();
			//mark
			if(AConfig._markC || AConfig._markP) {
				markAlpha = ( markAlpha + 230 ) % 255;
				int color = NovelPress._getColor(fileNow.fontColor,markAlpha);
				//p
				if(AConfig._markP && poseType=='p') {
					int mW = this.fontSize/2;
					int mH = this.fontSize/6;
					int mY = cY+this.fontSize-mH;
					for(int i=0; i<this.fontSize/4; i++) {
						fillRect(cX+i,mY-i,mW,mH,color);
					}
				}
				//c
				else if(AConfig._markC) {
					int mW = this.fontSize/2;
					int mH = this.fontSize/6;
					int mX = cX;
					int mY = cY+this.fontSize-mH;
					for(int i=0; i<this.fontSize/2; i++) {
						fillRect(mX+i,mY,mW-i,mH,color);
					}
				}
			}
			unlock();
			//setMenu(NovelPress.MENUSET_SCENARIO);
			System.gc();
			FUtil._sleep(100);
		}
	}
	private int scene_select(String[] top,boolean[] log,int timer) {
		//timer 時間制限 0:無し1以上:有り
		//return 選んだ選択肢番号
		//loadした時は-1を返す
		//タイムアウトの時は-2を返す
		//タイトルへ戻るときは-3を返す
		
		//maxW計算
		int maxW = 0;
		for(int i=0; i<top.length; i++) {
			int w = _stringWidth(top[i])+fileNow.fontSpaceW*(top[i].length()-1);
			if(w>maxW) maxW = w;
		}
		int shadeColor = FUtil._getRevColor(fileNow.fontColor);
		//メニュー内容を変更する
		setMenu(NovelPress.MENUSET_POSING);
		//タイムリミットを取得する
		long timerAnc = System.currentTimeMillis()+timer;
		//実行
		this.key = -1;
		while(true) {
			//キー
			int key = this.key;
			this.key = -1;
			//決定確認
			if(this.tchUp!=null) {
				int[] xy = transTouchXY(this.tchUp[0],this.tchUp[1]);
				for(int i=0; i<top.length; i++) {
					int y = cY+fileNow.fontSpaceH+(this.fontSize+fileNow.fontSpaceH)*i;
					if(xy[1]>=y && xy[1]<y+this.fontSize) {
						//メニューを変更する
						setMenu(NovelPress.MENUSET_NONE);
						//結果を返す
						this.tchUp = null;
						return i;
					}
				}
				this.tchUp = null;
			}
			//メニュー
			else if(key==NovelPress.KEY_MENU_MENU) {
				setVibration(false);
				((NovelPress)getContext()).startMyActivity(AMenu.class);
				io_saveConfig();
				if(this.fileNow.vib) setVibration(true);
			}
			//栞
			else if(key==NovelPress.KEY_MENU_BOOKMARK) {
				if(NovelPress._nooFile>0) {
					Intent intent = new Intent(getContext(),ABookmark.class);
					intent.putExtra(NovelPress.ITTKEY_BOOKMARK,this.fileNow.savable);
					//実行
					setVibration(false);
					intent = ((NovelPress)getContext()).startMyActivity(intent);
					if(intent!=null && intent.getExtras().getBoolean(NovelPress.ITTKEY_BOOKMARK)) {
						setFile();
						return -1;
					} else {
						if(this.fileNow.vib) setVibration(true);
					}
				}
			}
			//回想
			else if(key==NovelPress.KEY_MENU_READBACK) {
				if(this.sbBac.length()>0) {
					Intent itt = new Intent(getContext(),AReadback.class);
					itt.putExtra(NovelPress.ITTKEY_READBACK,this.sbBac.toString());
					setVibration(false);
					((NovelPress)getContext()).startMyActivity(itt);
					if(this.fileNow.vib) setVibration(true);
				}
			}
			//自動
			else if(key==NovelPress.KEY_MENU_AUTOREAD) {
				this.inAuto = !this.inAuto;
			}
			//テキストを消す
			else if(key==NovelPress.KEY_MENU_TXTOFF) {
				//メニューを消す
				setMenu(NovelPress.MENUSET_NONE);
				setVibration(false);
				//背景画像を生成する
				NPFile fileBuf = NPFile._getFileBuf();
				Bitmap img = (fileBuf==null)?getImage(this.fileNow):getImage(fileBuf);
				//タッチされるまで待機する
				this.tchUp = null;
				this.p.setAlpha(255);
				while(this.tchUp==null) {
					lock();
					this.can.drawBitmap(img,drawX,drawY,this.p);
					unlock();
				}
				//メニューを元に戻す
				setMenu(NovelPress.MENUSET_POSING);
				if(this.fileNow.vib) setVibration(true);
				this.tchUp = null;
			}
			//タイトルへ戻る
			else if(key==NovelPress.KEY_MENU_TITLE) {
				fileNow.index = s.length();
				return -3;
			}
			//タイムアウト判定
			if(timer>0) {
				if(System.currentTimeMillis()>timerAnc) return -2;
			}
			//描画
			lock();
			draw();
			for(int i=0; i<top.length; i++) {
				int type = (log!=null && i<log.length && log[i])?0:2;
				int y = cY+fileNow.fontSpaceH+(this.fontSize+fileNow.fontSpaceH)*i;
				if(cX>fileNow.txtareaX) y += this.fontSize;
				//タッチ確認
				boolean onFocus = false;
				for(int[] tch:this.tchNow) {
					int[] xy = transTouchXY(tch[1],tch[2]);
					if(xy[1]>=y && xy[1]<y+this.fontSize) {
						onFocus = true;
						break;
					}
				}
				if(onFocus) {
					int color = NovelPress._getColor(fileNow.fontColor,100);
					//通常選択肢
					if(timer<=0) {
						fillRect(fileNow.txtareaX-1,y-1,maxW+2,this.fontSize+2,color);
					}
					//タイムアウト選択肢
					else {
						int rx = fileNow.txtareaX-1;
						int ry = y-1;
						int rw = maxW+2;
						int rh = this.fontSize+2;
						drawRect(rx,ry,rw,rh,color);
						int tw = (rw-4)*(int)(timerAnc-System.currentTimeMillis())/timer;
						if(tw>0) fillRect(rx+rw-2-tw,ry+2,tw,rh-4,color);
					}
				}
				if(fileNow.fontSpaceW==0) drawString(top[i],fileNow.txtareaX,y,type,fileNow.fontColor,shadeColor);
				else {
					int cX = fileNow.txtareaX;
					for(int j=0; j<top[i].length(); j++) {
						String c = ""+top[i].charAt(j);
						drawString(c,cX,y,type,fileNow.fontColor,shadeColor);
						cX += _stringWidth(c)+fileNow.fontSpaceW;
					}
				}
			}
			unlock();
			//setMenu(NovelPress.MENUSET_SCENARIO);
			FUtil._sleep(100);
		}
	}
	private void setFile() {
		//実機を実行ファイル(fileNow)の状態にする
		copyToFileRec();
		sbNow.delete(0,sbNow.length());
		sbBac.delete(0,sbBac.length());
		cX = fileNow.txtareaX;
		cY = fileNow.txtareaY;
		setFontSize(fileNow.fontSize);
		setVibration(fileNow.vib);
		//サウンド
		SoundPlayer._allStop(0);
		if(this.fileNow.mld!=null) {
			for(int i=0; i<this.fileNow.mld.length; i++) {
				if(this.fileNow.mld[i]>=0) SoundPlayer._play(this.fileNow.mld[i],true,0);
			}
		}
	}

	//
	//描画
	//
	private void draw() {
		//背景
		NPFile fileBuf = NPFile._getFileBuf();
		if(fileBuf==null) {
			if(this.imgNow==null || this.fileNow.isNeedRedraw()) {
				this.imgNow = getImage(this.fileNow);
				this.fileNow.finRedraw();
			}
		}else {
			if(this.imgNow==null || fileBuf.isNeedRedraw()) {
				this.imgNow = getImage(fileBuf);
				fileBuf.finRedraw();
			}
		}
		this.can.drawBitmap(this.imgNow,drawX,drawY,null);
		//frame
		int alpha = fileNow.frameA>=0?fileNow.frameA:AConfig._frameA;
		if(alpha<10) {
			fillRect(
				this.fileNow.getFrameX(),
				this.fileNow.getFrameY(),
				this.fileNow.getFrameW(),
				this.fileNow.getFrameH(),
				NovelPress._getColor(this.fileNow.getFrameColor(),255*(10-alpha)/10));
		}
		//txt
		drawTxt();
	}
	private void drawTxt() {
		//描画中の文字が無い場合
		if(sbNow.length()==0) {
			//描画位置を修正して終了する
			cX = fileNow.txtareaX;
			cY = fileNow.txtareaY;
			return;
		}
		String txt = sbNow.toString();
		NPFile fileRec = NPFile._getFileRec();
		setFontSize(fileRec.fontSize);
		int txtareaX = fileNow.txtareaX;
		int txtareaY = fileNow.txtareaY;
		int txtareaW = fileNow.txtareaW;
		int fontColor = fileRec.fontColor;
		int fontSpaceW = fileRec.fontSpaceW;
		int fontSpaceH = fileRec.fontSpaceH;
		cX = txtareaX;
		cY = txtareaY;
		int index = 0;
		boolean inEscape = false;
		while(index<txt.length()) {
			char c = txt.charAt(index);
			//escape
			if(c==CHAR_ESCAPE) {
				if(inEscape==false) {
					inEscape = true;
					index++;
					continue;
				}
			}
			//タグ
			if(c==CHAR_TAG && inEscape==false) {
				char tag = txt.charAt(index+1);
				if(tag==CHAR_TAG_TXTCOLOR) {
					int r = txt.charAt(index+2);
					int g = txt.charAt(index+3);
					int b = txt.charAt(index+4);
					fontColor = FUtil._getColor(r,g,b);
				}
				if(tag==CHAR_TAG_TXTSIZE) {
					setFontSize((byte)(txt.charAt(index+2)&0xFF));
				}
				if(tag==CHAR_TAG_TXTSPACE) {
					fontSpaceW = (byte)(txt.charAt(index+2)&0xff);
					fontSpaceH = (byte)(txt.charAt(index+3)&0xff);
				}
				index = txt.indexOf(CHAR_TAG,index+1);
			}else {
				if(c==CHAR_LF) {
					cX = txtareaX;
					cY += this.fontSize+fontSpaceH;
					//if(cY+getFontH()>txtareaY+txtareaH) break;
				}else {
					int cW = _stringWidth(""+c);
					if(cX+fontSpaceW+cW>txtareaX+txtareaW) {
						cX = txtareaX;
						cY += this.fontSize+fontSpaceH;
						//if(cY+getFontH()>txtareaY+txtareaH) break;
					}
					if(cX>txtareaX) cX += fontSpaceW;
					drawString(""+c,cX,cY,0,fontColor,0);
					cX += cW;
				}
			}
			index++;
			inEscape = false;
		}
	}
	//文字描画
	private void drawString(String str,int x,int y,int type,int color,int color2) {
		//影付き
		if(type==1) {
			drawString(str,x+1,y+1,NovelPress._getColor(color2));
			drawString(str,x,y,NovelPress._getColor(color));
		}
		//枠付き
		else if(type==2) {
			for(int i=0; i<9; i++) {
				if(i!=4) drawString(str,x-1+(i%3),y-1+(i/3),NovelPress._getColor(color2));
			}
			drawString(str,x,y,NovelPress._getColor(color));
		}
		//プレーン
		else {
			drawString(str,x,y,NovelPress._getColor(color));
		}
	}
	//ソフトラベル描画
	private int menuSetBuf = -1;
	private void setMenu(int menuSet) {
		if(menuSet==this.menuSetBuf) return;
		this.menuSetBuf = menuSet;
		//メニュー文字列を生成する
		Vector<Integer> vec = new Vector<Integer>();
		switch(menuSet) {
		case NovelPress.MENUSET_TITLE:
			vec.add(new Integer(NovelPress.KEY_MENU_END));
			if(NovelPress._url!=null) vec.add(new Integer(NovelPress.KEY_MENU_SITE));
			break;
		case NovelPress.MENUSET_POSING:
			vec.add(new Integer(NovelPress.KEY_MENU_MENU));
			vec.add(new Integer(NovelPress.KEY_MENU_BOOKMARK));
			vec.add(new Integer(NovelPress.KEY_MENU_READBACK));
			if(NovelPress._autable) vec.add(new Integer(NovelPress.KEY_MENU_AUTOREAD));
			vec.add(new Integer(NovelPress.KEY_MENU_TXTOFF));
			vec.add(new Integer(NovelPress.KEY_MENU_TITLE));
			break;
		case NovelPress.MENUSET_NONE:
			break;
		}
		int[] menu = new int[vec.size()];
		for(int i=0; i<menu.length; i++) menu[i] = vec.elementAt(i).intValue();
		//メニュー文字列をセットする
		((NovelPress)getContext()).setMenuSet(menu);
	}
	//一行コメント描画
	/*private void drawComment(String str) {
		if(str==null || str.length()==0) return;
		int color = _getColor(this.sysColor_font2);
		fillRect(0,getHeight()-this.fontSize*2-1,getWidth(),this.fontSize+2,color);
		drawRect(-1,getHeight()-this.fontSize*2-3,getWidth()+2,this.fontSize+6,color);
		drawString(str,(getWidth()-_stringWidth(str))/2,getHeight()-this.fontSize,0,this.sysColor_font,0);
	}*/
	//スクロールバー描画
	/*private void drawScrollBar(int y,int h,int len,int onePage,int index) {
		int bY = y+h*index/len;
		int bH = h*onePage/len+1;
		fillRect(getWidth()-3,bY,3,bH,_getColor(this.sysColor_font2));
	}*/
	/*private void drawScrollBarVert(int x,int w,int len,int onePage,int index) {
		int bX = (w*(len-(index+onePage))/len)-x;
		int bW = w*onePage/len+1;
		fillRect(bX,getHeight()-3,bW,3,_getColor(this.sysColor_font2));
	}*/

	//
	//システム画面
	//
	//メニュー画面
	/*private void scene_menu(boolean fromTitle) {
		int com = 0;
		String[] top = {"【設定】","【CG-List】","【ED-List】","【BGM-List】"};
		int[] topNo;
		int[] cX;
		
		setFont(1);
		setVibration(false);
		gotoTitle = false;
		//topNo
		int nooTop = 1;
		if(modeCGList)  nooTop++;
		if(modeEDList)  nooTop++;
		if(modeBGMList) nooTop++;
		topNo = new int[nooTop];
		nooTop = 1;
		if(modeCGList)  topNo[nooTop++] = 1;
		if(modeEDList)  topNo[nooTop++] = 2;
		if(modeBGMList) topNo[nooTop  ] = 3;
		//cX
		cX = new int[topNo.length];
		int cW = 0;
		for(int i=0; i<cX.length; i++) {
			cW += stringWidth(top[topNo[i]]);
		}
		int gap = (getWidth()-cW)/(topNo.length+1);
		cW = gap;
		for(int i=0; i<cX.length; i++) {
			cX[i] = cW;
			cW += stringWidth(top[topNo[i]])+gap;
		}
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				break;
			}else if(ks[Display.KEY_SOFT2]==1){
				if(fromTitle==false) {
					if(confirm("タイトル画面へ戻りますか？")) {
						gotoTitle = true;
						return;
					}
				}
			}else if(ks[Display.KEY_LEFT]>0) {
				com = (com+topNo.length-1)%topNo.length;
			}else if(ks[Display.KEY_RIGHT]>0) {
				com = (com+1)%topNo.length;
			}else if(ks[Display.KEY_SELECT]==1) {
				if(topNo[com]==0) scene_menu_config(false);
				if(topNo[com]==1) scene_menu_cg();
				if(topNo[com]==2) scene_menu_ed();
				if(topNo[com]==3) scene_menu_bgm();
				if(gotoTitle) return;
			}
			//描画
			lock();
			fillRect(this.sysColor_back);
			g.setColor(getColor(this.sysColor_font,255));
			g.drawLine(0,24,getWidth(),24);
			for(int i=0; i<topNo.length; i++) {
				drawString(top[topNo[i]],cX[i],24-(24-getFontH())/2,0,(i==com)?this.sysColor_font:this.sysColor_font2,0);
			}
			unlock();
			drawSlbl(LABEL_BACK,fromTitle?null:LABEL_TITLE);
			FUtil._sleep(100);
			//設定直行
			if(modeCGList==false && modeEDList==false && modeBGMList==false) {
				scene_menu_config(fromTitle==false);
				break;
			}
		}
		//復元
		if(fromTitle==false) {
			setFont(fileNow.fontSize);
		}
	}
	//設定画面
	private void scene_menu_config(boolean directAcs) {
		int i,j;
		int com = 0;
		int	nooItem;
		int volumeNo = -1;
		String[][] top = {
			{"文字の表示速度","←遅い　速い→"},
			{"???????の????","←短い　長い→"},
			{"文字枠の透明度","←不透明　透明→　"},
			{"音量","←小さい　大きい→"},
			{"??????????",null},
			{"????待ちの表示",null},
			{"改頁待ちの表示",null}};
		boolean[] able = {ciTxtSpeed,ciAutoWait,ciFrameA,ciVolume,ciVibable,ciMarkC,ciMarkP};
		int[] item = {txtSpeed,autoWait,frameA,volume,(vibable)?1001:1000,(markC)?1001:1000,(markP)?1001:1000};
		
		//項目セット
		nooItem = 0;
		for(i=0; i<able.length; i++) {
			if(able[i]) {
				top[nooItem] = top[i];
				item[nooItem] = item[i];
				if(top[i][0]=="音量") volumeNo = nooItem;
				nooItem++;
			}
		}
		if(nooItem==0) return;
		//バーの色計算
		int[] barR = new int[2];
		barR[0] = this.sysColor_back>>16&0xff;
		barR[1] = (this.sysColor_font>>16&0xff)-barR[0];
		int[] barG = new int[2];
		barG[0] = this.sysColor_back>>8&0xff;
		barG[1] = (this.sysColor_font>>8&0xff)-barG[0];
		int[] barB = new int[2];
		barB[0] = this.sysColor_back&0xff;
		barB[1] = (this.sysColor_font&0xff)-barB[0];
		//実行
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				break;
			}else if(ks[Display.KEY_SOFT2]==1){
				if(directAcs) {
					Image img = g.getImage(0,0,getWidth(),getHeight());
					if(confirm("タイトル画面へ戻りますか？")) {
						gotoTitle = true;
						return;
					}
					g.drawImage(img,0,0);
				}
			}else if(ks[Display.KEY_UP]>0) {
				com = (com+nooItem-1)%nooItem;
			}else if(ks[Display.KEY_DOWN]>0) {
				com = (com+1)%nooItem;
			}else if(ks[Display.KEY_RIGHT]>0) {
				if(item[com]<1000) {
					if(item[com]<10) item[com]++;
				}else {
					item[com] = 1000;
				}
			}else if(ks[Display.KEY_LEFT]>0) {
				if(item[com]<1000) {
					if(item[com]>0) item[com]--;
				}else {
					item[com] = 1001;
				}
			}else if(ks[Display.KEY_SELECT]==1) {
				if(item[com]<1000) {
					if(item[com]<10) item[com]++;
				}else {
					item[com] = (item[com]==1000)?1001:1000;
				}
			}
			//描画
			lock();
			g.clearRect(0,24+12,getWidth(),getHeight()-(24+12));
			g.setColor(getColor(this.sysColor_cur,255));
			g.fillRect(0,24+12+getFontH()*com+1,getWidth(),getFontH()-2);
			for(i=0; i<nooItem; i++) {
				int cX = 120;
				int cY = 24+12+getFontH()*(i+1);
				drawString(top[i][0],12,cY,1,this.sysColor_font,this.sysColor_font2);
				if(item[i]<1000) {
					int w = getWidth()-(cX+12);
					w -= w%10;
					g.setColor(getColor(this.sysColor_font,255));
					g.drawRect(cX-1,cY-getFontH()+1,w+1,getFontH()-3);
					for(j=0; j<item[i]; j++) {
						g.setColor(getColor((barR[0]+barR[1]*(j+1)/10)<<16|(barG[0]+barG[1]*(j+1)/10)<<8|(barB[0]+barB[1]*(j+1)/10),255));
						g.fillRect(cX+(w/10)*j+1,cY-getFontH()+3,w/10-2,getFontH()-6);
					}
				}else {
					drawString("ON" ,cX,   cY,0,(item[i]==1001)?this.sysColor_font:this.sysColor_font2,0);
					drawString("OFF",cX+24,cY,0,(item[i]==1000)?this.sysColor_font:this.sysColor_font2,0);
				}
			}
			drawComment(top[com][1]);
			unlock();
			drawSlbl(LABEL_BACK,directAcs?LABEL_TITLE:null);
			//音量変更
			if(com==volumeNo) {
				SoundPlayer._setVolume(item[com]*10);
			}
			FUtil._sleep(100);
		}
		//終了処理
		i = 0;
		if(ciTxtSpeed)  txtSpeed  = item[i++];
		if(ciAutoWait)  autoWait  = item[i++];
		if(ciFrameA)    frameA    = item[i++];
		if(ciVolume)    volume    = item[i++];
		if(ciVibable)   vibable   = (item[i++]==1001);
		if(ciMarkC)     markC     = (item[i++]==1001);
		if(ciMarkP)     markP     = (item[i++]==1001);
		io_saveConfig();
	}
	//CG画面
	private void scene_menu_cg() {
		int com = 0;
		int[] cgNo = new int[nooCGList];
		Image[] cg = new Image[3];
		boolean reset = true;
		String comment = "<<  ●  >>";
			
		if(nooCGList==0) return;
		//cgNo
		for(int i=0; i<cgNo.length; i++) {
			cgNo[i] = -1;
			if(flagCGList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[cg,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index = s.indexOf(',',index+4)+1;
						cgNo[i] = FUtil._parseNum(s.substring(index,s.indexOf("]",index)));
						break;
					}
				}
			}
		}
		//サムネのサイズを計算する
		int thumW = getWidth()/3;
		int thumH = getHeight()/3;
		//実行
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				return;
			}else if(ks[Display.KEY_RIGHT]>0) {
				com = (com+1)%nooCGList;
				reset = true;
			}else if(ks[Display.KEY_LEFT]>0) {
				com = (com+nooCGList-1)%nooCGList;
				reset = true;
			}else if(ks[Display.KEY_SELECT]==1) {
				if(cg[1]!=null) {
					Image fullImage = g.getImage(0,0,getWidth(),getHeight());
					lock();
					fillRect(this.sysColor_back);
					g.drawImage(cg[1],0,0);
					unlock();
					drawSlbl(null,null);
					keyStateClear();
					while(true) {
						ks = getKeyState();
						if(ks[Display.KEY_SELECT]==1) break;
					}
					g.drawImage(fullImage,0,0);
				}
			}
			//cg生成
			if(reset) {
				for(int i=0; i<3; i++) {
					int imgNo = cgNo[(com+nooCGList-1+i)%nooCGList];
					if(imgNo>=0) {
						cg[i] = getImage(true,imgNo);
					}else {
						cg[i] = null;
					}
				}
				reset = false;
			}
			//描画
			lock();
			g.clearRect(0,24+12,getWidth(),getHeight()-(24+12));
			int wY = (getHeight()-thumH)/2;
			for(int i=0; i<3; i++) {
				int wX = -thumW/2+((getWidth()-thumW*2)/2+thumW)*i;
				g.setColor(getColor(this.sysColor_font2,255));
				g.drawRect(wX-2,wY-2,thumW+3,thumH+3);
				if(cg[i]==null) {
					g.fillRect(wX,wY,thumW,thumH);
				}else {
					g.drawScaledImage(cg[i],wX,wY,thumW,thumH,0,0,cg[i].getWidth(),cg[i].getHeight());
				}
			}
			String str = "No."+(com+1);
			drawString(str,(getWidth()-stringWidth(str))/2,wY-getFontH(),1,this.sysColor_font,this.sysColor_font2);
			drawComment(comment);
			unlock();
			drawSlbl(LABEL_BACK,null);
			FUtil._sleep(100);
		}
	}
	//ED画面
	private void scene_menu_ed() {
		int startNo = 0;
		int max = (getHeight()-(24+12))/getFontH()-1;
		String[] comment;
		
		if(nooEDList==0) return;
		//comment
		comment = new String[nooEDList];
		for(int i=0; i<nooEDList; i++) {
			if(flagEDList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[z,"+FUtil._formatNum(i+1,j)+",");
					if(index<0) index = s.indexOf("[Z,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index = s.indexOf(',',index+3);
						comment[i] = s.substring(index+1,s.indexOf(']',index));
						break;
					}
				}
			}
		}
		//実行
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				return;
			}
			if(ks[Display.KEY_UP]>0) {
				if(ks[Display.KEY_UP]==1 && startNo==0) startNo = nooEDList-max;
				else startNo--;
			}
			if(ks[Display.KEY_DOWN]>0) {
				if(ks[Display.KEY_DOWN]==1 && startNo==nooEDList-max) startNo = 0;
				else startNo++;
			}
			if(ks[Display.KEY_RIGHT]>0) {
				if(ks[Display.KEY_RIGHT]==1 && startNo==nooEDList-max) startNo = 0;
				else startNo += max;
			}
			if(ks[Display.KEY_LEFT]>0) {
				if(ks[Display.KEY_LEFT]==1 && startNo==0) startNo = nooEDList-max;
				else startNo -= max;
			}
			if(startNo>nooEDList-max) startNo = nooEDList-max;
			if(startNo<0) startNo=0;
			//描画
			lock();
			g.clearRect(0,24+12,getWidth(),getHeight()-(24+12));
			if(nooEDList>max) drawScrollBar(24+12,getHeight()-(24+12+12),nooEDList,max,startNo);
			int i = 0;
			while(true) {
				int cY = 24+12+getFontH()*(i+1);
				int edNo = startNo+i;
				if(edNo>=nooEDList || i>=max) break;
				//top
				drawString("No."+FUtil._formatNum(( startNo+i+1),(""+nooEDList).length()),12,cY,1,this.sysColor_font,this.sysColor_font2);
				//comment
				if(comment[startNo+i]==null) {
					drawString("---",60,cY,0,this.sysColor_font2,0);
				}else {
					drawString(comment[startNo+i],60,cY,0,this.sysColor_font,0);
				}
				i++;
			}
			unlock();
			drawSlbl(LABEL_BACK,null);
			FUtil._sleep(100);
		}
	}
	//BGM画面
	private void scene_menu_bgm() {
		int com = 0;
		int max = (getHeight()-(24+12))/getFontH()-3;
		String playingCom = null;
		boolean played = false;
		
		if(nooBGMList==0) return;
		//bgmNo comment
		int[] bgmNo = new int[nooBGMList];
		String[] comment = new String[nooBGMList];
		for(int i=0; i<nooBGMList; i++) {
			bgmNo[i] = -1;
			if(flagBGMList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[bgm,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index++;
						int indexEnd = s.indexOf("]",index);
						if(indexEnd>=0) {
							String[] ss = FUtil._splitString(s.substring(index,indexEnd),',');
							if(ss.length==4) {
								bgmNo[i]   = FUtil._parseNum(ss[2]);
								comment[i] = ss[3];
							}
						}
						break;
					}
				}
			}
		}
		//演奏状態保存
		int[] bgmState = SoundPlayer._getList(LIMIT_NOOMLD);
		//実行
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				//演奏状態復元
				if(played) {
					SoundPlayer._allStop(0);
					if(bgmState!=null) {
						for(int i=0; i<bgmState.length; i++) {
							if(bgmState[i]>=0) SoundPlayer._play(bgmState[i],true,0);
						}
					}
				}
				return;
			}
			if(ks[Display.KEY_SELECT]==1) {
				if(bgmNo[com]>=0) {
					SoundPlayer._allStop(0);
					SoundPlayer._play(bgmNo[com],true,0);
					playingCom = comment[com];
					played = true;
				}
			}
			if(ks[Display.KEY_UP]>0) {
				if(ks[Display.KEY_UP]==1 && com==0) com = nooBGMList-1;
				else com--;
			}
			if(ks[Display.KEY_DOWN]>0) {
				if(ks[Display.KEY_DOWN]==1 && com==nooBGMList-1) com = 0;
				else com++;
			}
			if(ks[Display.KEY_RIGHT]>0) {
				if(ks[Display.KEY_RIGHT]==1 && com==nooBGMList-1) com = 0;
				else com += max;
			}
			if(ks[Display.KEY_LEFT]>0) {
				if(ks[Display.KEY_LEFT]==1 && com==0) com = nooBGMList-1;
				else com -= max;
			}
			if(com>nooBGMList-1) com = nooBGMList-1;
			if(com<0) com = 0;
			//startNo計算
			int startNo = com-max/2;
			if(startNo>nooBGMList-max) startNo = nooBGMList-max;
			if(startNo<0) startNo = 0;
			//描画
			lock();
			g.clearRect(0,24+12,getWidth(),getHeight()-(24+12));
			if(nooBGMList>max) drawScrollBar(24+12,getFontH()*max,nooBGMList,max,startNo);
			int i = 0;
			while(true) {
				int cY = 24+12+getFontH()*(i+1);
				int index = startNo+i;
				if(index>=nooBGMList || i>=max) break;
				//com
				if(index==com) {
					g.setColor(getColor(this.sysColor_cur,255));
					g.fillRect(11,cY-getFontH(),getWidth()-26,getFontH());
				}
				//top
				drawString("No."+FUtil._formatNum(( startNo+i+1),(""+nooBGMList).length()),12,cY,1,this.sysColor_font,this.sysColor_font2);
				//comment
				if(comment[startNo+i]==null) {
					drawString("---",60,cY,0,this.sysColor_font2,0);
				}else {
					drawString(comment[startNo+i],60,cY,0,this.sysColor_font,0);
				}
				i++;
			}
			drawComment((playingCom!=null)?"? "+playingCom:" ");
			unlock();
			drawSlbl(LABEL_BACK,null);
			FUtil._sleep(100);
		}
	}
	//栞画面
	private boolean scene_record(boolean fromTitle) {
		//return loadしたかどうか
		int com = 0;
		boolean save_load;
		boolean savable;
		Calendar cal = Calendar.getInstance();
		
		//挙動制御
		if(nooFile==0) return false;
		if(fromTitle) {
			savable   = false;
			save_load = false;
		}else {
			savable   = this.fileNow.savable;
			save_load = savable;
		}
		setFont(1);
		setVibration(false);
		//time取得
		long[] time = new long[nooFile];
		for(int i = 0; i < nooFile; i++) {
			try{
				DataInputStream dataIn = Connector.openDataInputStream("scratchpad:///0;pos="+(SPPOS_FILE+NPFile.SP_SUM*i));
				time[i] = dataIn.readLong();
				dataIn.close();
			}catch(Exception e) {}
		}
		//実行
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_SOFT2]==1 || ks[Display.KEY_CLEAR]==1) {
				if(fromTitle) {
					if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) break;
				}else {
					if(ks[Display.KEY_SOFT2]==1 || ks[Display.KEY_CLEAR]==1) break;
				}
			}else if(ks[Display.KEY_LEFT]==1) {
				if(savable) {
					save_load = true;
				}
			}else if(ks[Display.KEY_RIGHT]==1) {
				save_load = false;
			}else if(ks[Display.KEY_UP]>0) {
				if(ks[Display.KEY_UP]==1 && com==0) com = nooFile-1;
				else if(com>0) com--;
			}else if(ks[Display.KEY_DOWN]>0) {
				if(ks[Display.KEY_DOWN]==1 && com==nooFile-1) com = 0;
				else if(com<nooFile-1) com++;
			}else if(ks[Display.KEY_SELECT]==1) {
				if(save_load==false && time[com]==0L) continue;
				String str = "File."+(com+1)+" ";
				if(save_load) {
					if(time[com]==0L) str += "にセーブしますか？";
					else str += "に上書きしますか？";
					if(confirm(str)) {
						io_saveFile(com);
						break;
					}
				}else {
					str += "をロードしますか？";
					if(confirm(str)) {
						io_loadFile(com);
						return true;
					}
				}
			}
			//描画
			lock();
			fillRect(this.sysColor_back);
			int cW = stringWidth("【SAVE】");
			int gap = (getWidth()-cW*2)/3;
			drawString("【SAVE】",gap,24-(24-getFontH())/2,0,save_load?this.sysColor_font:this.sysColor_font2,0);
			drawString("【LOAD】",gap*2+cW,24-(24-getFontH())/2,0,save_load?this.sysColor_font2:this.sysColor_font,0);
			g.setColor(getColor(this.sysColor_font,255));
			g.drawLine(0,24,getWidth(),24);
			g.setColor(getColor(this.sysColor_cur,255));
			g.fillRect(0,24+12+getFontH()*com+1,getWidth(),getFontH()-2);
			for(int i=0; i<nooFile; i++) {
				int cY = 24+12+getFontH()*(i+1);
				drawString("File." + FUtil._formatNum((i+1),(nooFile>=10)?2:1),12,cY,1,this.sysColor_font,this.sysColor_font2);
				if(time[i]==0L) {
					drawString("---",100,cY,0,this.sysColor_font2,0);
				}else {
					cal.setTime(new Date(time[i]));
					String str =
						FUtil._formatNum(cal.get(Calendar.MONTH)+1,2)    +"/"+
						FUtil._formatNum(cal.get(Calendar.DATE),2)       +" "+
						FUtil._formatNum(cal.get(Calendar.HOUR_OF_DAY),2)+":"+
						FUtil._formatNum(cal.get(Calendar.MINUTE),2);
					drawString(str,100,cY,0,this.sysColor_font,0);
				}
			}
			unlock();
			drawSlbl(fromTitle?LABEL_BACK:null,fromTitle?null:LABEL_BACK);
			FUtil._sleep(100);
		}
		//復元
		if(fromTitle==false) {
			setFont(fileNow.fontSize);
		}
		return false;
	}
	//STAFF画面
	private void scene_staff() {
		final int
			ALIGN_LEFT   = 0,
			ALIGN_CENTER = 1,
			ALIGN_RIGHT  = 2;
		String top = "【"+titleMenu[3]+"】";
		
		byte[] data = null;
		if(resType==RESTYPE_IN) data = getResFromRes("txt/staff.txt");
		else                    data = getResFromJar("txt/staff.txt",null);
		if(data==null) return;
		String str = FUtil._deleteStr(new String(data),"\n\r\t");
		if(str==null) return;
		//パース
		Vector vecLine = new Vector();
		Vector vecOpts = new Vector();
		StringBuffer sb = new StringBuffer();
		int align = ALIGN_LEFT;
		int fontSize = 1;
		int fontColor = this.sysColor_font;
		setFont(1);
		int[] opt = new int[]{align,0,getFontH(),fontSize,fontColor};
		int index = 0;
		while(index<str.length()) {
			char c = str.charAt(index);
			//タグ
			if(c=='[') {
				index++;
				int index2 = str.indexOf(']',index);
				if(index<0) return;
				String[] token = FUtil._splitString(str.substring(index,index2),',');
				String tag = token[0];
				//align
				if(tag.equals("align")) {
					//行頭でのみ有効
					if(opt[1]==0) {
						if(token[1].equals("l")) align = ALIGN_LEFT;
						if(token[1].equals("c")) align = ALIGN_CENTER;
						if(token[1].equals("r")) align = ALIGN_RIGHT;
						opt[0] = align;
					}
				}
				//l
				if(tag.equals("l")) {
					//格納
					vecLine.addElement(sb.toString());
					vecOpts.addElement(opt);
					//初期化
					opt = new int[5];
					opt[0] = align;
					opt[2] = getFontH();
					opt[3] = fontSize;
					opt[4] = fontColor;
					sb.delete(0,sb.length());
				}
				//txtsize
				if(tag.equals("txtsize")) {
					fontSize = FUtil._formatNum(FUtil._parseNum(token[1]),1,5);
					setFont(fontSize);
					//行頭の場合は初期Sizeとして扱う
					if(opt[1]==0) {
						opt[2] = getFontH();
						opt[3] = fontSize;
					}else {
						sb.append(""+CHAR_TAG+CHAR_TAG_TXTSIZE+(char)fontSize+CHAR_TAG);
					}
				}
				//txtcolor
				if(tag.equals("txtcolor")) {
					int r = (fontColor>>16)&0xff;
					int g = (fontColor>> 8)&0xff;
					int b = (fontColor    )&0xff;
					if(token[1]!=null) r = FUtil._formatNum(FUtil._parseNum(token[1]),0,255);
					if(token[2]!=null) g = FUtil._formatNum(FUtil._parseNum(token[2]),0,255);
					if(token[3]!=null) b = FUtil._formatNum(FUtil._parseNum(token[3]),0,255);
					fontColor = FUtil._getColor(r,g,b);
					//行頭の場合は初期colorとして扱う
					if(opt[1]==0) {
						opt[4] = fontColor;
					}else {
						sb.append(
							""+CHAR_TAG+CHAR_TAG_TXTCOLOR+
							(char)r+
							(char)g+
							(char)b+
							CHAR_TAG);
					}
				}
				index = index2+1;
				continue;
			}
			//通常の文字
			sb.append(c);
			opt[1] += stringWidth(""+c);
			opt[2] = Math.max(opt[2],getFontH());
			index++;
		}
		if(sb.length()>0) {
			vecLine.addElement(sb.toString());
			vecOpts.addElement(opt);
		}
		//変換
		int nooLine = vecLine.size();
		String[] line = new String[nooLine];
		int[][]  opts = new int[nooLine][];
		for(int i=0; i<nooLine; i++) {
			line[i] = (String)vecLine.elementAt(i);
			opts[i] = (int[])vecOpts.elementAt(i);
		}
		sb = null;
		vecLine = null;
		vecOpts = null;
		//サイズ等計算
		int sumH = 0;
		for(int i=0; i<nooLine; i++) {
			sumH += opts[i][2];
		}
		int wH = (getHeight()-(24+1));
		boolean overH = false;
		if(sumH>wH) {
			overH = true;
		}
		//実行
		int sY = 24+1;
		int speed = 1;
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_SOFT1]==1 || ks[Display.KEY_CLEAR]==1) {
				return;
			}else if(ks[Display.KEY_UP]>0) {
				if(overH) {
					sY += speed;
				}
			}else if(ks[Display.KEY_DOWN]>0) {
				if(overH) {
					sY -= speed;
				}
			}else if(ks[Display.KEY_RIGHT]>0) {
				//ページ切り替え?
			}else if(ks[Display.KEY_LEFT]>0) {
				//ページ切り替え?
			}
			if(this.anyState) {
				speed = speed<<1;
				if(speed>20) speed = 20;
			}else {
				speed = 1;
			}
			if(overH) {
				if(sY<getHeight()-sumH) sY = getHeight()-sumH;
				if(sY>24+1) sY = 24+1;
			}
			//描画
			lock();
			fillRect(this.sysColor_back);
			g.setColor(getColor(this.sysColor_font,255));
			g.drawLine(0,24,getWidth(),24);
			setFont(1);
			drawString(top,(getWidth()-stringWidth(top))/2,24-(24-getFontH())/2,0,this.sysColor_font,0);
			if(overH) {
				drawScrollBar(24+1,wH,sumH,wH,24+1-sY);
			}
			g.setClip(6,24,getWidth()-12,wH);
			int lineY = sY;
			for(int i=0; i<nooLine; i++) {
				//画面外判定
				if(lineY+opts[i][2]<=24+1) {
					lineY += opts[i][2];
					continue;
				}
				if(lineY>getHeight()) break;
				//描画
				if(opts[i][1]>0) {
					int sX = 6;
					if(opts[i][0]==ALIGN_CENTER) sX = (getWidth()-opts[i][1])/2;
					if(opts[i][0]==ALIGN_RIGHT)  sX = getWidth()-6-opts[i][1];
					setFont(opts[i][3]);
					int color = opts[i][4];
					index = 0;
					while(index<line[i].length()) {
						char c = line[i].charAt(index);
						String cc = ""+c;
						//タグ
						if(c==CHAR_TAG) {
							index++;
							char tag = line[i].charAt(index);
							//txtsize
							if(tag==CHAR_TAG_TXTSIZE) {
								setFont(line[i].charAt(index+1));
							}
							//txtcolor
							if(tag==CHAR_TAG_TXTCOLOR) {
								int r = line[i].charAt(index+1);
								int g = line[i].charAt(index+2);
								int b = line[i].charAt(index+3);
								color = FUtil._getColor(r,g,b);
							}
							index = line[i].indexOf(CHAR_TAG,index)+1;
							continue;
						}
						//通常の文字
						drawString(cc,sX,lineY+opts[i][2],0,color,0);
						sX += stringWidth(cc);
						if(sX>=getWidth()-6) break;
						index++;
					}
				}
				lineY += opts[i][2];
			}
			g.clearClip();
			unlock();
			drawSlbl(LABEL_BACK,null);
			FUtil._sleep(100);
		}
	}*/
	//回想画面
/*	private void scene_readback() {
		int startNo;
		int gap = 6;
		String[] lines;
		
		//txt取得
		if(sbBac.length()==0 && sbNow.length()==0) return;
		String txt = sbBac.toString()+sbNow.toString();
		//分割
		setFont(1);
		setVibration(false);
		Vector vec = new Vector();
		StringBuffer sb = new StringBuffer();
		int windowLimit = (this.isVert)?getHeight():getWidth();
		int lineW = 0;
		int index = 0;
		boolean inEscape = false;
		while(index<txt.length()) {
			char c = txt.charAt(index);
			//escape
			if(c==CHAR_ESCAPE) {
				if(inEscape==false) {
					inEscape = true;
					index++;
					continue;
				}
			}
			if(c==CHAR_TAG && inEscape==false) {
				//タグ
				index = txt.indexOf(CHAR_TAG,index+1);
			}else if(c==CHAR_LF) {
				//改行
				if(sb.length()>0) {
					vec.addElement(sb.toString());
					sb.delete(0,sb.length());
				}else vec.addElement(null);
				lineW = 0;
			}else {
				//テキスト
				int cW = stringWidth(""+c);
				if(lineW+cW>windowLimit-gap*2) {
					if(sb.length()>0) {
						vec.addElement(sb.toString());
						sb.delete(0,sb.length());
					}else vec.addElement(null);
					lineW = 0;
				}
				sb.append(c);
				lineW += cW;
			}
			index++;
			inEscape = false;
		}
		if(sb.length()>0) vec.addElement(sb.toString());
		if(vec.size()==0) {
			return;
		}
		lines = new String[vec.size()];
		vec.copyInto(lines);
		sb = null;
		vec = null;
		txt = null;
		//最大行数計算
		int max = (((this.isVert)?getWidth():getHeight())-gap*2)/getFontH();
		//実行
		startNo = lines.length-max;
		drawSlbl(null,null);
		keyStateClear();
		while(true) {
			//キー
			int[] ks = getKeyState();
			if(ks[Display.KEY_UP]>0) {
				if(this.isVert) startNo -= max;
				else            startNo--;
			}else if(ks[Display.KEY_DOWN]>0) {
				if(this.isVert) startNo += max;
				else            startNo++;
			}else if(ks[Display.KEY_RIGHT]>0) {
				if(this.isVert) startNo--;
				else            startNo += max;
			}else if(ks[Display.KEY_LEFT]>0) {
				if(this.isVert) startNo++;
				else            startNo -= max;
			}else if(anyPush) {
				return;
			}
			if(startNo>lines.length-max) startNo = lines.length-max;
			if(startNo<0) startNo = 0;
			//描画
			lock();
			fillRect(this.sysColor_back);
			if(lines.length>max) {
				if(this.isVert) {
					drawScrollBarVert(0,getWidth(),lines.length,max,startNo);
				}else {
					drawScrollBar(0,getHeight(),lines.length,max,startNo);
				}
			}
			index = 0;
			while(true) {
				int lineNo = startNo+index;
				if(index>=max || lineNo>=lines.length) break;
				if(lines[lineNo]!=null) {
					if(this.isVert) {
						drawStringVert(lines[lineNo],getWidth()-(gap+getFontH()*(index+1)),gap,0,this.sysColor_font,0);
					}else {
						drawString(lines[lineNo],gap,gap+getFontH()*(index+1),0,this.sysColor_font,0);
					}
				}
				index++;
			}
			unlock();
			FUtil._sleep(100);
		}
	}*/

	//
	//記録
	//
	//global系
	private void io_saveGlobal() {
		DataOutputStream dataOut = null;
		try {
			byte[] b = FUtil._boolToByte(flagGlobal);
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"gflag.dat"));
			dataOut.write(b);
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveGlobal",null,e);
		}
	}
	private void io_saveConfig() {
		DataOutputStream dataOut = null;
		try {
			//ディレクトリを生成する
			File dir = new File(NovelPress._SDHome);
			if(dir.exists()==false) dir.mkdir();
			//configファイルを書き込む
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"config.dat"));
			dataOut.writeByte(AConfig._txtSpeed);
			dataOut.writeByte(AConfig._autoWait);
			dataOut.writeByte(AConfig._frameA);
			dataOut.writeByte(AConfig._volume);
			dataOut.writeBoolean(AConfig._vibable);
			dataOut.writeBoolean(AConfig._markC);
			dataOut.writeBoolean(AConfig._markP);
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveConfig",null,e);
		}
	}
	private void io_saveCG() {
		DataOutputStream dataOut = null;
		try {
			byte[] b = FUtil._boolToByte(flagCGList);
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"cglist.dat"));
			dataOut.write(b);
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveCG",null,e);
		}
	}
	private void io_saveED() {
		DataOutputStream dataOut = null;
		try {
			byte[] b = FUtil._boolToByte(flagEDList);
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"edlist.dat"));
			dataOut.write(b);
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveED",null,e);
		}
	}
	private void io_saveBGM() {
		DataOutputStream dataOut = null;
		try {
			byte[] b = FUtil._boolToByte(flagBGMList);
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"bgmlist.dat"));
			dataOut.write(b);
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveBGM",null,e);
		}
	}
	private void io_saveSelectLog() {
		DataOutputStream dataOut = null;
		try {
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"selectlog.dat"));
			dataOut.writeInt(selectLog.size());
			for(int i=0; i<selectLog.size(); i++) {
				int[] slog = (int[])selectLog.elementAt(i);
				dataOut.writeInt(slog[0]);
				dataOut.writeShort(slog[1]);
			}
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveSelectLog",null,e);
		}
	}
	private void io_saveOutput() {
		DataOutputStream dataOut = null;
		try {
			dataOut = new DataOutputStream(new FileOutputStream(NovelPress._SDHome+"output.dat"));
			for(int i=0; i<output.length; i++) {
				if(output[i]==null) {
					dataOut.write(0);
				}else {
					byte[] data = output[i].getBytes();
					dataOut.write(data.length);
					dataOut.write(data);
				}
			}
			dataOut.close();
		}catch(Exception e) {
			NovelPress.showErrDialog("io_saveOutput",null,e);
		}
	}

	//
	//その他
	//
	//バイブ
	private void setVibration(boolean on_off) {
		if(on_off && AConfig._vibable) {
			((Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(60*1000);
		}else {
			((Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE)).cancel();		
		}
	}
	//ラベル検索
	private int indexLabel(String str) {
		if(str==null) return -1;
		String label = str;
		if(label.startsWith("@")==false) label = "@"+label;
		int index = 0;
		while(true) {
			index = s.indexOf("["+label+"]",index);
			if(index<0) break;
			if(index==0 || s.charAt(index-1)!=CHAR_ESCAPE) break;
			index++;
		}
		return index;
	}
	public Bitmap getImage(NPFile file) {
		//img生成
		Bitmap off = Bitmap.createBitmap(this.drawW,this.drawH,Bitmap.Config.ARGB_8888);
		Canvas offCan = new Canvas(off);
		//背景
		NPImage imageB = file.getImageB();
		if(imageB==null) {
			offCan.drawColor(NovelPress._getColor(file.getImageBColor()));
		} else {
			offCan.drawColor(NovelPress._getColor(NovelPress._sysColor_back));
			Bitmap img = imageB.getImage();
			if(img!=null) {
				offCan.drawBitmap(
					img,
					new Rect(0,0,img.getWidth(),img.getHeight()),
					new Rect(0,0,img.getWidth()*drawRate/_RATE,img.getHeight()*drawRate/_RATE),
					null);
			}
		}
		//前景
		NPImage[] imageF = file.getImageF();
		for(int i=0; i<imageF.length; i++) {
			if(imageF[i]==null) continue;
			Bitmap img = imageF[i].getImage();
			if(img!=null) {
				int x = imageF[i].getX()*drawRate/_RATE;
				int y = imageF[i].getY()*drawRate/_RATE;
				int w = img.getWidth()*drawRate/_RATE;
				int h = img.getHeight()*drawRate/_RATE;
				offCan.drawBitmap(
					img,
					new Rect(0,0,img.getWidth(),img.getHeight()),
					new Rect(x,y,x+w,y+h),
					null);
			}
		}
		//色調変更
		if(file.effect!=NPFile.EFFECT_NONE) {
			int[] rgbs = new int[off.getWidth()*off.getHeight()];
			off.getPixels(rgbs,0,off.getWidth(),0,0,off.getWidth(),off.getHeight());
			for(int i=0; i<rgbs.length; i++) {
				int r = Color.red  (rgbs[i]);
				int g = Color.green(rgbs[i]);
				int b = Color.blue (rgbs[i]);
				//モノクロ
				if(file.effect==NPFile.EFFECT_MONO) {
					//int c = ((rgbs[i]>>16&0xff)+(rgbs[i]>>8&0xff)+(rgbs[i]&0xff))/3;//平均値
					int c = (r*2+g*4+b)/7;//NTSC近似
					rgbs[i] = Color.rgb(c,c,c);
				}
				//セピア
				if(file.effect==NPFile.EFFECT_SEPIA) {
					int c = (r*2+g*4+b)/7;//NTSC近似
					rgbs[i] = Color.rgb(c*245/255,c*220/255,c*155/255);//245,220,155 若干暗いか？
				}
				//反転
				if(file.effect==NPFile.EFFECT_CREV) {
					rgbs[i] = Color.rgb(255-r,255-g,255-b);
				}
			}
			off.setPixels(rgbs,0,off.getWidth(),0,0,off.getWidth(),off.getHeight());
		}
		return off;
	}
	protected void onKeyDown(int keyCode) {
		this.key = keyCode;
	}
	private void copyToFileRec() {
		NPFile fileNow = NPFile._getFileNow();
		NPFile fileRec = NPFile._getFileRec();
		fileRec.init();
		fileRec.index      = fileNow.index;
		fileRec.txtareaX   = fileNow.txtareaX;
		fileRec.txtareaY   = fileNow.txtareaY;
		fileRec.txtareaW   = fileNow.txtareaW;
		fileRec.txtareaH   = fileNow.txtareaH;
		fileRec.fontSpeed  = fileNow.fontSpeed;
		fileRec.fontSize   = fileNow.fontSize;
		fileRec.fontColor  = fileNow.fontColor;
		fileRec.fontSpaceW = fileNow.fontSpaceW;
		fileRec.fontSpaceH = fileNow.fontSpaceH;
		fileRec.frameX     = fileNow.frameX;
		fileRec.frameY     = fileNow.frameY;
		fileRec.frameW     = fileNow.frameW;
		fileRec.frameH     = fileNow.frameH;
		fileRec.frameColor = fileNow.frameColor;
		fileRec.frameA     = fileNow.frameA;
		fileRec.vib        = fileNow.vib;
		fileRec.effect     = fileNow.effect;
		fileRec.savable    = fileNow.savable;
		if(fileNow.bi==null) {
			fileRec.biColor = fileNow.biColor;
		}else {
			fileRec.bi = fileNow.bi.clone();
		}
		for(int i=0; i<fileNow.fi.length; i++) {
			fileRec.fi[i] = fileNow.fi[i].clone();
		}
		fileRec.mld = SoundPlayer._getList(NPCan.LIMIT_NOOMLD);
		System.arraycopy(fileNow.var,0,fileRec.var,0,fileNow.var.length);
	}
	private void copyToFileBuf() {
		NPFile._createFileBuf();
		NPFile fileNow = NPFile._getFileNow();
		NPFile fileBuf = NPFile._getFileBuf();
		fileBuf.effect = fileNow.effect;
		if(fileNow.bi==null) {
			fileBuf.biColor = fileNow.biColor;
		}else {
			fileBuf.bi = fileNow.bi.clone();
		}
		for(int i=0; i<fileNow.fi.length; i++) {
			fileBuf.fi[i] = fileNow.fi[i].clone();
		}
	}
	protected int[] getCGList() {
		int[] list = new int[NovelPress._nooCGList];
		for(int i=0; i<list.length; i++) {
			list[i] = -1;
			if(flagCGList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[cg,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index = s.indexOf(',',index+4)+1;
						list[i] = FUtil._parseNum(s.substring(index,s.indexOf("]",index)));
						break;
					}
				}
			}
		}
		return list;
	}
	protected String[] getEDList() {
		String[] list = new String[NovelPress._nooEDList];
		for(int i=0; i<list.length; i++) {
			if(flagEDList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[z,"+FUtil._formatNum(i+1,j)+",");
					if(index<0) index = s.indexOf("[Z,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index = s.indexOf(',',index+3);
						list[i] = s.substring(index+1,s.indexOf(']',index));
						break;
					}
				}
			}
		}
		return list;
	}
	protected Object[][] getBGMList() {
		Object[][] list = new Object[NovelPress._nooBGMList][2];
		for(int i=0; i<list.length; i++) {
			int bgmNo = -1;
			String title = null;
			if(this.flagBGMList[i]) {
				for(int j=(""+(i+1)).length(); j<=3; j++) {
					int index = s.indexOf("[bgm,"+FUtil._formatNum(i+1,j)+",");
					if(index>=0) {
						index++;
						int indexEnd = s.indexOf("]",index);
						if(indexEnd>=0) {
							String[] ss = FUtil._splitString(s.substring(index,indexEnd),',');
							if(ss.length==4) {
								bgmNo = FUtil._parseNum(ss[2]);
								title = ss[3];
							}
						}
						break;
					}
				}
			}
			list[i][0] = new Integer(bgmNo);
			list[i][1] = title;
		}
		return list;
	}
	private void lock() {
		//画面の幅と高さが変更されていないか確認する
		if(getWidth()!=this.wW || getHeight()!=this.wH) {
			setWindowSize(getWidth(),getHeight());
			this.imgNow = null;
		}
		//canvasを取得する
		this.can = getHolder().lockCanvas();
		//黒で塗りつぶす
		this.can.drawColor(NovelPress._getColor(NovelPress._sysColor_back));
		//clip領域を設定する
		this.can.clipRect(new Rect(this.drawX,this.drawY,this.drawX+this.drawW,this.drawY+this.drawH));
	}
	private void unlock() {
		getHolder().unlockCanvasAndPost(this.can);
	}
	
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	//定数
	public static int
		WIDTH,
		HEIGHT;
	public static final char
		CHAR_ESCAPE = '\\',
		CHAR_LF     = '\n',
		CHAR_TAG    = '\t',
		CHAR_TAG_TXTSIZE  = '0',
		CHAR_TAG_TXTCOLOR = '1',
		CHAR_TAG_TXTSPACE = '2',
		CHAR_TAG_BOXCOLOR = '3';
	public static final int
		LIMIT_NOOFILE      = 15,
		LIMIT_NOOCGLIST    = 100,
		LIMIT_NOOEDLIST    = 100,
		LIMIT_NOOBGMLIST   = 100,
		LIMIT_NOOSELECTLOG = 100,
		LIMIT_NOOOUTPUT    = 20,
		LIMIT_NOOFI        = 10,
		LIMIT_NOOMLD       = 5,
		LIMIT_RESTORE      = 10000;
	//
	private static NPCan _npcan;
	
	protected static NPCan _getInstance() {
		return _npcan;
	}
	//ime
	private String ime(final String str) {
		//入力フォームを生成する
		final EditText form = new EditText(getContext());
		if(str!=null) form.setText(str);
		//ダイアログを生成して表示する
		this.imeFinFlg = false;
		this.imeHandler.post(new Runnable(){
			public void run() {
				new AlertDialog.Builder(getContext())
					.setView(form)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							imeStr = form.getText().toString();
							imeFinFlg = true;
						}
					})
					.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							imeStr = str;
							imeFinFlg = true;
						}
					})
					.setCancelable(false)
					.show();
			}
		});
		//待機する
		while(this.imeFinFlg==false);
		return this.imeStr;
	}
	public static String getTxt() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<100; i++) {
			String name = "txt/"+FUtil._formatNum(i,2);
			String s = NovelPress._loadText(name);
			if(s!=null) sb.append(s);
		}
		if(sb.length()==0) return null;
		return sb.toString();
	}
}


