package novel.press;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AReadback extends Activity {
	private LinearLayout allBase;
	private ScrollView txtBase;
	private TextView txt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//回想文字列を取得する
		String str = getIntent().getExtras().getString(NovelPress.ITTKEY_READBACK);
		if(str==null) finish();
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//基盤
		this.allBase = new LinearLayout(this);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
			//テキスト基盤
			this.txtBase = new ScrollView(this);
			this.txtBase.setScrollBarStyle(ScrollView.SCROLLBARS_INSIDE_INSET);
			this.txtBase.post(new Runnable() {
				public void run() {
					txtBase.fullScroll(View.FOCUS_DOWN);
				}
			});
				//テキスト
				this.txt = new TextView(this);
				this.txt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
				this.txt.setPadding(6,6,6,6);
				this.txt.setText(str);
				this.txt.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
		//紐付ける
		this.txtBase.addView(this.txt);
		this.allBase.addView(this.txtBase);
		setContentView(this.allBase);
	}
}
