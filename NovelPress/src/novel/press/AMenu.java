package novel.press;

import java.util.Vector;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class AMenu extends TabActivity {
	private Vector<TextView> inds = new Vector<TextView>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//
		_tabH = ((int)new TextView(this).getTextSize())*4;
		//ホスト
		TabHost host = getTabHost();
		host.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_back));
		host.setCurrentTab(0);
		//設定
		if(true) {
			TextView ind = createInd(NovelPress.STR_MENUTOP_CONFIG);
			changeIndState(ind,true);
			host.addTab(createTab(host,AConfig.class,ind));
			this.inds.addElement(ind);
		}
		//CGリスト
		if(NovelPress._modeCGList) {
			TextView ind = createInd(NovelPress.STR_MENUTOP_CGLIST);
			changeIndState(ind,false);
			host.addTab(createTab(host,ACGList.class,ind));
			this.inds.addElement(ind);
		}
		//EDリスト
		if(NovelPress._modeEDList) {
			TextView ind = createInd(NovelPress.STR_MENUTOP_EDLIST);
			changeIndState(ind,false);
			host.addTab(createTab(host,AEDList.class,ind));
			this.inds.addElement(ind);
		}
		//BGMリスト
		if(NovelPress._modeBGMList) {
			TextView ind = createInd(NovelPress.STR_MENUTOP_BGMLIST);
			changeIndState(ind,false);
			host.addTab(createTab(host,ABGMList.class,ind));
			this.inds.addElement(ind);
		}
		//リスナ
		host.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				for(int i=0; i<inds.size(); i++) {
					boolean hasFocus = (inds.elementAt(i).getText()==tabId);
					changeIndState(inds.elementAt(i),hasFocus);
				}
			}
		});
	}
	private TextView createInd(String str) {
		TextView ind = new TextView(this);
		ind.setText(str);
		ind.setMinHeight(_tabH);
		ind.setGravity(Gravity.CENTER);
		return ind;
	}
	private TabSpec createTab(TabHost host,Class<? extends Activity> cls,TextView ind) {
		return host
			.newTabSpec((String)ind.getText())
			.setIndicator(ind)
			.setContent(new Intent(this,cls).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));		
	}
	private void changeIndState(TextView ind,boolean hasFocus) {
		GradientDrawable grad;
		if(hasFocus) {
			ind.setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
			grad = new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
				NovelPress._getColor(NovelPress._sysColor_cur),
				NovelPress._getColor(NovelPress._sysColor_back)});
		} else {
			ind.setTextColor(NovelPress._getColor(NovelPress._sysColor_font2));
			grad = new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
				NovelPress._getColor(NovelPress._sysColor_cur2),
				NovelPress._getColor(NovelPress._sysColor_back)});
		}
		grad.setCornerRadii(new float[]{10,10,10,10,0,0,0,0});
		grad.setStroke(1,NovelPress._getColor(NovelPress._sysColor_back));
		ind.setBackgroundDrawable(grad);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getCurrentActivity().onConfigurationChanged(newConfig);
	}
	//////////////////////////////////////////////////
	// static
	//////////////////////////////////////////////////
	protected static int _tabH;
}
