package novel.press;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

public class NovelPress extends Activity {
	//文字列定数
	protected static final String
		STR_MENULBL_END          = "終了",
		STR_MENULBL_SITE         = "サイト",
		STR_MENULBL_MENU         = "メニュー",
		STR_MENULBL_BOOKMARK     = "栞",
		STR_MENULBL_READBACK     = "回想",
		STR_MENULBL_AUTOREAD     = "オート",
		STR_MENULBL_TXTOFF       = "テキスト消去",
		STR_MENULBL_TITLE        = "タイトル",
		STR_MENUTOP_CONFIG       = "設定",
		STR_MENUTOP_CGLIST       = "CG-List",
		STR_MENUTOP_EDLIST       = "ED-List",
		STR_MENUTOP_BGMLIST      = "BGM-List",
		STR_NUMIND_BOOKMARK      = "File.",
		STR_NUMIND_CGLIST        = "No.",
		STR_NUMIND_EDLIST        = "No.",
		STR_NUMIND_BGMLIST       = "No.",
		STR_OFFLBL_BOOKMARK      = "---",
		STR_OFFLBL_EDLIST        = "---",
		STR_OFFLBL_BGMLIST       = "---",
		STR_BTNLBL_BOOKMARK_SAVE = "SAVE",
		STR_BTNLBL_BOOKMARK_LOAD = "LOAD",
		STR_BTNLBL_BGMLIST_PLAY  = "?";
	
	
	
	private NPCan can;
	private boolean BackActivityFlag;
	private Intent BackActivityIntent;
	private boolean gotoMyActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//assetsへのアクセス用
		_novelpress = this;
		_am = this.getAssets();
		_handler = new Handler();
		//configファイルを読み込む
		_loadConfig();
		//SDカードの保存先を設定する
		_SDHome = Environment.getExternalStorageDirectory().getPath()+"/"+getPackageName()+"/";
		//viewをセットする
		this.can = new NPCan(this);
		setContentView(this.can);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		this.can.onKeyDown(mi.getItemId());
		return true;
	}
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent intent) {
		if(requestCode==BACKACTIVITYCODE) {
			this.BackActivityFlag   = true;
			this.BackActivityIntent = intent;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK) {
			this.can.onKeyDown(KEY_BACK);
			return true;
		}
		return false;
	}
	@Override
	public void onDestroy() {
		SoundPlayer._allStop(0);
		super.onDestroy();
	}
	protected Intent startMyActivity(Intent intent) {
		this.gotoMyActivity = true;
		startActivityForResult(intent,NovelPress.BACKACTIVITYCODE);
		this.BackActivityFlag = false;
		while(true) {
			if(this.BackActivityFlag) {
				this.BackActivityFlag = false;
				this.gotoMyActivity = false;
				break;
			}
			FUtil._sleep(1000);
		}
		return this.BackActivityIntent;
	}
	protected Intent startMyActivity(Class<? extends Activity> cls) {
		Intent intent = new Intent(this,cls);
		return startMyActivity(intent);
	}
	private int[] menuIDs;
	protected void setMenuSet(int[] menuIDs) {
		this.menuIDs = menuIDs;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int len = menu.size();
		for(int i=0; i<len; i++) {
			MenuItem mi = menu.getItem(i);
			int id = mi.getItemId();
			boolean visible = false;
			for(int j=0; j<this.menuIDs.length; j++) {
				if(this.menuIDs[j]==id) {
					visible = true;
					break;
				}
			}
			mi.setVisible(visible);
		}
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi;
		mi = menu.add(0,KEY_MENU_END,     0,STR_MENULBL_END);
		mi.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		mi = menu.add(0,KEY_MENU_SITE,    0,STR_MENULBL_SITE);
		mi.setIcon(android.R.drawable.ic_menu_info_details);
		mi = menu.add(0,KEY_MENU_MENU,    0,STR_MENULBL_MENU);
		mi.setIcon(android.R.drawable.ic_menu_agenda);
		mi = menu.add(0,KEY_MENU_BOOKMARK,0,STR_MENULBL_BOOKMARK);
		mi.setIcon(android.R.drawable.ic_menu_save);
		mi = menu.add(0,KEY_MENU_READBACK,0,STR_MENULBL_READBACK);
		mi.setIcon(android.R.drawable.ic_menu_recent_history);
		mi = menu.add(0,KEY_MENU_AUTOREAD,0,STR_MENULBL_AUTOREAD);
		mi.setIcon(android.R.drawable.ic_menu_rotate);
		mi = menu.add(0,KEY_MENU_TXTOFF,  0,STR_MENULBL_TXTOFF);
		mi.setIcon(android.R.drawable.ic_menu_gallery);
		mi = menu.add(0,KEY_MENU_TITLE,   0,STR_MENULBL_TITLE);
		mi.setIcon(android.R.drawable.ic_menu_revert);
		return true;
	}
	public void onStop() {
		super.onStop();
		//自作activity行きじゃなかったらアプリを終了する
		if(this.gotoMyActivity==false) {
			Process.killProcess(Process.myPid());
		}
	}

	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	protected static final int BACKACTIVITYCODE = 9;
	protected static final int
		MENUSET_TITLE    = 0,
		MENUSET_POSING   = 1,
		MENUSET_NONE     = 999;
	protected static final int
		KEY_BACK          = 0,
		KEY_MENU_END      = 1,
		KEY_MENU_SITE     = 2,
		KEY_MENU_MENU     = 3,
		KEY_MENU_BOOKMARK = 4,
		KEY_MENU_READBACK = 5,
		KEY_MENU_AUTOREAD = 6,
		KEY_MENU_TXTOFF   = 7,
		KEY_MENU_TITLE    = 8;
	protected static final String
		ITTKEY_BOOKMARK        = "key_bookmark",
		ITTKEY_READBACK        = "key_readback",
		ITTKEY_CONFIG_TXTSPEED = "key_txtSpeed",
		ITTKEY_CONFIG_AUTOWAIT = "key_autoWait",
		ITTKEY_CONFIG_FRAMEA   = "key_frameA",
		ITTKEY_CONFIG_VOLUME   = "key_volume",
		ITTKEY_CONFIG_VIBABLE  = "key_vibable",
		ITTKEY_CONFIG_MARKC    = "key_markC",
		ITTKEY_CONFIG_MARKP    = "key_markP",
		ITTKEY_CGLIST_FULLDRAW = "key_cg_full";
	protected static NovelPress _novelpress;
	protected static AssetManager _am;
	protected static Handler _handler;
	//作品設定関連
	protected static int
		_nooFile,
		_nooRes;
	protected static String
		_appName,
		_url;
	//タイトル画面関連
	protected static boolean
		_titleBGM;
	protected static int
		_titleBGMFadein,
		_titleMenuPos,
		_titleMenuSize,
		_titleMenuMgn,//左0 中1 右2
		_titleMenuX,
		_titleMenuY;
	protected static String[]
		_titleMenu = new String[]{
	       	"START",
	       	"LOAD",
	       	"MENU",
	       	"STAFF"};
	//config項目
	protected static boolean
		_ciTxtSpeed,
		_ciAutoWait,
		_ciFrameA,
		_ciVolume,
		_ciVibable,
		_ciMarkC,
		_ciMarkP;
	protected static int
		_iniTxtSpeed,
		_iniAutoWait,
		_iniVolume,
		_iniFrameA;
	protected static boolean
		_iniVibable,
		_iniMarkC,
		_iniMarkP;
	//オートプレイ関連
	protected static boolean
		_autable;
	//リスト関連
	protected static boolean
		_modeCGList,
		_modeEDList,
		_modeBGMList;
	protected static int
		_nooCGList,
		_nooEDList,
		_nooBGMList;
	//システム画面の色
	protected static int
		_sysColor_back,
		_sysColor_font,
		_sysColor_font2,
		_sysColor_cur,
		_sysColor_cur2;
	protected static boolean
		_selectLogable;
	protected static String _SDHome;
	//エラーログ関連
	public static boolean _errlogable;
	public static String _errlogMess;

	//作品設定用紙読み込み
	private static void _loadConfig() {
		//設定用紙読み込み
		String cp = FUtil._deleteStr(NovelPress._loadText("config"),"\n\r\t");//フォーマット（改行?タブ）
		if(cp==null) return;
		//分割
		String[] cps = FUtil._splitString(cp,';');
		if(cps==null) return;
		cp = null;
		Vector<String[]> vec = new Vector<String[]>();
		for(int i=0; i<cps.length; i++) {
			String[] item = FUtil._splitString(cps[i],'=');
			if(item!=null) vec.addElement(item);
		}
		if(vec.size()==0) return;
		cps = null;
		//適応
		while(vec.size()>0) {
			String[] item = (String[])(vec.elementAt(0));
			vec.removeElementAt(0);
			item[0] = FUtil._deleteStr(item[0]," ");//半角空白
			if(item[0]==null) {	
			}else if(item[0].equals("appName")) {
				_appName = item[1];
			}else if(item[0].equals("url")) {
				_url = item[1];
			}else if(item[0].equals("titleBGM")) {
				_titleBGM = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("titleBGMFadein")) {
				_titleBGMFadein = FUtil._parseNum(item[1]);
			}else if(item[0].equals("titleMenuStart")) {
				_titleMenu[0] = item[1];
			}else if(item[0].equals("titleMenuLoad")) {
				_titleMenu[1] = item[1];
			}else if(item[0].equals("titleMenuMenu")) {
				_titleMenu[2] = item[1];
			}else if(item[0].equals("titleMenuStaff")) {
				_titleMenu[3] = item[1];
			}else if(item[0].equals("titleMenuX")) {
				_titleMenuX = FUtil._parseNum(item[1]);
			}else if(item[0].equals("titleMenuY")) {
				_titleMenuY = FUtil._parseNum(item[1]);
			}else if(item[0].equals("titleMenuPos")) {
				if(item[1]!=null) {
					if(item[1].equals("left"  )) _titleMenuPos = 0;
					if(item[1].equals("center")) _titleMenuPos = 1;
					if(item[1].equals("right" )) _titleMenuPos = 2;
				}
			}else if(item[0].equals("titleMenuSize")) {
				_titleMenuSize = FUtil._parseNum(item[1]);
			}else if(item[0].equals("titleMenuMgn")) {
				_titleMenuMgn = FUtil._parseNum(item[1]);
			}else if(item[0].equals("modeCG")) {
				_modeCGList = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("modeED")) {
				_modeEDList = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("modeBGM")) {
				_modeBGMList = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("autable")) {
				_autable = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("selectLog")) {
				_selectLogable = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("nooFile")) {
				_nooFile = FUtil._parseNum(item[1]);
			}else if(item[0].equals("ciTxtSpeed")) {
				_ciTxtSpeed = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciAutoWait")) {
				_ciAutoWait = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciFrameA")) {
				_ciFrameA = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciVolume")) {
				_ciVolume = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciVibable")) {
				_ciVibable = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciMarkC")) {
				_ciMarkC = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("ciMarkP")) {
				_ciMarkP = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("iniTxtSpeed")) {
				_iniTxtSpeed = FUtil._parseNum(item[1]);
			}else if(item[0].equals("iniAutoWait")) {
				_iniAutoWait = FUtil._parseNum(item[1]);
			}else if(item[0].equals("iniFrameA")) {
				_iniFrameA = FUtil._parseNum(item[1]);
			}else if(item[0].equals("iniVolume")) {
				_iniVolume = FUtil._parseNum(item[1]);
			}else if(item[0].equals("iniVibable")) {
				_iniVibable = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("iniMarkC")) {
				_iniMarkC = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("iniMarkP")) {
				_iniMarkP = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("nooCG")) {
				_nooCGList = FUtil._parseNum(item[1]);
			}else if(item[0].equals("nooED")) {
				_nooEDList = FUtil._parseNum(item[1]);
			}else if(item[0].equals("nooBGM")) {
				_nooBGMList = FUtil._parseNum(item[1]);
			}else if(item[0].equals("nooRes")) {
				_nooRes = FUtil._parseNum(item[1]);
			}else if(item[0].equals("sysColorBack")) {
				_sysColor_back = Integer.parseInt(item[1],16);
			}else if(item[0].equals("sysColorFont")) {
				_sysColor_font = Integer.parseInt(item[1],16);
				//文字の影色取得(背景色3文字色1)
				_sysColor_font2 = FUtil._getMidColor(_sysColor_back,_sysColor_font);
				_sysColor_font2 = FUtil._getMidColor(_sysColor_back,_sysColor_font2);
			}else if(item[0].equals("sysColorCur")) {
				_sysColor_cur = Integer.parseInt(item[1],16);
				//カーソルの影色取得（背景色1カーソル色1）
				_sysColor_cur2 = FUtil._getMidColor(_sysColor_back,_sysColor_cur);
			}else if(item[0].equals("errlogable")) {
				_errlogable = (item[1]!=null && item[1].equals("on"));
			}else if(item[0].equals("errlogMess")) {
				_errlogMess = item[1];
			}
		}
	}
	public static String _loadText(String name) {
		byte[] data = null;
		try {
			InputStream in = _am.open(name+".txt");
			int size = in.available();
			data = new byte[size];
			in.read(data);
			in.close();
		} catch(Exception e) {
			return null;
		}
		try{
			return new String(data,"utf8"/*"Shift_JIS"*/);
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}
	public static Bitmap _loadImage(String name) {
		String[] loc = new String[]{".png",".jpg",".gif"};
		
		for(int i=0; i<loc.length; i++) {
			try {
				InputStream in = _am.open(name+loc[i]);
				Bitmap image = BitmapFactory.decodeStream(in);
				in.close();
				return image;
			} catch(Exception e) {}
		}
		return null;
	}
	/*protected static byte[] _loadResource(String name,boolean showErr) {
		try {
			InputStream in = _am.open(name);
			int size = in.available();
			byte[] data = new byte[size];
			in.read(data);
			in.close();
			return data;
		} catch(Exception e) {
			if(showErr) {
				NovelPress.showErrDialog("_getResource",name,e);
			}
			return null;
		}
	}*/
	protected static int _getColor(int color) {
		//0xRRGGBB -> android.Color
		return Color.rgb((color>>16)&0xFF,(color>>8)&0xFF,color&0xFF);
	}
	protected static int _getColor(int color,int alpha) {
		//0xRRGGBB -> android.Color
		return Color.argb(alpha&0xFF,(color>>16)&0xFF,(color>>8)&0xFF,color&0xFF);
	}

	private static boolean _dialogwait;
	protected static void showErrDialog(final String loc,final String opt,final Exception e) {
		//コンソールへ出力する
		//System.out.println("@Loc:"+loc+"()");
		//System.out.println("@Opt:"+opt);
		//if(e!=null) e.printStackTrace();

		if(_errlogable==false) return;
		//ダイアログを生成して表示する
		_dialogwait = true;
		_handler.post(new Runnable(){
			public void run() {
				EditText form = new EditText(_novelpress);
				form.setText(
					"> Loc :"+loc+"()\n"+
					"> Type:"+e+"\n"+
					"> Opt :"+opt+"\n"+
					"> Mess:"+(e==null?"null":e.getMessage())+"\n"+
					((_errlogMess==null)?"":_errlogMess));
				new AlertDialog.Builder(_novelpress)
					.setTitle("ERROR")
					.setMessage("実行中にエラーが発生しました。")
					.setView(form)
					.setPositiveButton("続行", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							_dialogwait = false;
						}
					})
					.setCancelable(false)
					.show();
			}
		});
		//待機する
		while(_dialogwait);
	}
}

/*
 * ver :06.13.00
 * date:12,02,10
 * 正式公開
 * エラーログ出力機能を実装
 * BGMリストで同じ曲を連続再生をしないように変更
 * 固定文字列をNovelPressクラスの定数にした
 * 
 * ver :06.13.01
 * date:12,02,28
 * アプリの終了をfinish()からProcess.killProcess()に変更
 * エラーログの出力内容を一部変更
 * 
 */

/*
 * TODO
 * FUtilをNovelPressへ統合する
 * NPCanvasviewをNPCanへ統合する
 * STAFF画面。[align,c]の場合にはみ出た文字の省略を制御する
 * 設定画面。シークバーのつまみが左右端で消える。パディング等で調整する
 * 改頁待ちのマークを変更する
 * マルチタッチ機能を削る。NPにはおそらく不要
 * メニューが出るのを速くする。予め作っておくか？
 * SDが無い場合は端末内部に保存するようにする
 * スクロールバーをsyscolorに対応させる
 * BGM画面、再生中の曲をクリックすると停止するように
 * 各activityでのバックキーの挙動を再考する
 * 描画率が整数だから、文字列描画のときとかに誤差が目立つことがある。floatにするか？
 * サウンド再生が重い。フェード時にのみスレッドに入るようにする
 * タイムアウト選択肢、フォーカス当てないと残り時間が見えない
 * ホームキー?サイトジャンプから復帰できるようにする。NPCanを完全な描画専用スレッドにして通常のライフサイクルを適用しないと無理か？
 *
 */
