package novel.press;


import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AEDList extends Activity {	
	private ScrollView allBase;
	private TableLayout allBase2;
	private TableRow[] line;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//ラインの高さ
		int lineH = ((int)new TextView(this).getTextSize())*2;
		//基盤レイアウト
		this.allBase = new ScrollView(this);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
		this.allBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
		this.allBase2 = new TableLayout(this);
		this.allBase2.setColumnStretchable(1,true);
			//リスト
			String[] list = NPCan._getInstance().getEDList();
			this.line = new TableRow[list.length];
			for(int i=0; i<list.length; i++) {
				//リスト番号
				TextView top = new TextView(this);
				top.setHeight(lineH);
				top.setGravity(Gravity.CENTER_VERTICAL);
				top.setPadding(10,0,0,0);
				top.setText(NovelPress.STR_NUMIND_EDLIST+FUtil._formatNum(i+1,(""+NovelPress._nooEDList).length()));
				top.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				ShapeDrawable topBD = new ShapeDrawable(new RoundRectShape(new float[]{10,10,0,0,0,0,10,10},null,null));
				topBD.getPaint().setColor(NovelPress._getColor(NovelPress._sysColor_cur,100));
				top.setBackgroundDrawable(topBD);
				//リスト文字列
				TextView com = new TextView(this);
				com.setHeight(lineH);
				com.setGravity(Gravity.CENTER_VERTICAL);
				com.setPadding(30,0,0,0);
				if(list[i]==null) {
					com.setText(NovelPress.STR_OFFLBL_EDLIST);
					com.setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
				} else {
					com.setText(list[i]);
					com.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				}
				ShapeDrawable comBD = new ShapeDrawable(new RoundRectShape(new float[]{0,0,10,10,10,10,0,0},null,null));
				comBD.getPaint().setColor(NovelPress._getColor(NovelPress._sysColor_cur,100));
				com.setBackgroundDrawable(comBD);
				//リスト
				this.line[i] = new TableRow(this);
				this.line[i].addView(top);
				this.line[i].addView(com);
				TableLayout.LayoutParams lp = new TableLayout.LayoutParams();
				lp.setMargins(1,1,0,1);
				this.line[i].setLayoutParams(lp);
			}
		//紐付ける
		for(int i=0; i<this.line.length; i++) {
			this.allBase2.addView(this.line[i]);
		}
		this.allBase.addView(this.allBase2);
		setContentView(this.allBase);
	}

}

