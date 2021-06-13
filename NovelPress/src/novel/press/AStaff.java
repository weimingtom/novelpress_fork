package novel.press;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AStaff extends Activity {
	private ScrollView allBase;
	private LinearLayout allBase2;
	//解析用
	private LinearLayout line;
	private StringBuffer sb;
	private int
		align,
		fontSize,
		fontColor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//STAFFファイルを読み込む
		String str = FUtil._deleteStr(NovelPress._loadText("txt/staff"),"\n\r\t");;
		if(str==null) return;
		//基盤
		this.allBase = new ScrollView(this);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
		this.allBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
		this.allBase2 = new LinearLayout(this);
		this.allBase2.setOrientation(LinearLayout.VERTICAL);
		this.allBase2.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.FILL_PARENT,ScrollView.LayoutParams.FILL_PARENT));
			//中身
			this.sb        = new StringBuffer();
			this.align     = Gravity.LEFT;
			this.fontSize  = 12;
			this.fontColor = NovelPress._sysColor_font;
			this.line = new LinearLayout(this);
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
						if(this.sb.length()==0) {
							if(token[1].equals("l")) this.align = Gravity.LEFT;
							if(token[1].equals("c")) this.align = Gravity.CENTER_HORIZONTAL;
							if(token[1].equals("r")) this.align = Gravity.RIGHT;
						}
					}
					//l
					if(tag.equals("l")) {
						//格納
						addCom();
						addLine();
						this.sb.delete(0,this.sb.length());
					}
					//txtsize
					if(tag.equals("txtsize")) {
						int size = FUtil._parseNum(token[1]);
						if(this.fontSize!=size) {
							if(this.sb.length()>0) {
								addCom();
								this.sb.delete(0,this.sb.length());
							}
							this.fontSize = size;
						}
					}
					//txtcolor
					if(tag.equals("txtcolor")) {
						int r = (this.fontColor>>16)&0xff;
						int g = (this.fontColor>> 8)&0xff;
						int b = (this.fontColor    )&0xff;
						if(token[1]!=null) r = FUtil._formatNum(FUtil._parseNum(token[1]),0,255);
						if(token[2]!=null) g = FUtil._formatNum(FUtil._parseNum(token[2]),0,255);
						if(token[3]!=null) b = FUtil._formatNum(FUtil._parseNum(token[3]),0,255);
						int color = FUtil._getColor(r,g,b);
						if(this.fontColor!=color) {
							if(this.sb.length()>0) {
								addCom();
								this.sb.delete(0,this.sb.length());
							}
							this.fontColor = color;
						}
					}
					index = index2+1;
					continue;
				}
				//通常の文字
				this.sb.append(c);
				index++;
			}
			//残りを格納する
			if(this.sb.length()>0) addCom();
			if(this.line.getChildCount()>0) addLine();
		//紐付ける
		this.allBase.addView(this.allBase2);
		setContentView(this.allBase);
	}
	private void addCom() {
		TextView com = new TextView(this);
		com.setSingleLine();
		com.setEllipsize(TruncateAt.START);
		com.setText((this.sb.length()==0)?" ":this.sb.toString());
		com.setTextSize(this.fontSize);
		com.setTextColor(NovelPress._getColor(this.fontColor));
		this.line.addView(com);
	}
	private void addLine() {
		//ラインにalignを設定する
		this.line.setGravity(Gravity.BOTTOM|this.align);
		//はみ出た文字の省略部分を設定する
		/*TruncateAt ell = null;
		if(this.align==Gravity.LEFT)              ell = TruncateAt.END;
		if(this.align==Gravity.CENTER_HORIZONTAL) ell = TruncateAt.MIDDLE;
		if(this.align==Gravity.RIGHT)             ell = TruncateAt.START;
		for(int i=0; i<this.line.getChildCount(); i++) {
			((TextView)this.line.getChildAt(i)).setEllipsize(ell);
		}*/
		//ラインを追加する
		this.allBase2.addView(this.line);
		//ラインを初期化する
		this.line = new LinearLayout(this);
	}
}
