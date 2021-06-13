package novel.press;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ACGList extends Activity implements OnClickListener {
	private HorizontalScrollView allBase;
	private LinearLayout allBase2;
	private LinearLayout[] lineBase;
	private TextView[] lineTop;
	private View[] lineImg;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//
		//基盤
		this.allBase = new HorizontalScrollView(this);
		this.allBase.setBackgroundDrawable(new GradientDrawable(Orientation.TOP_BOTTOM,new int[]{
			NovelPress._getColor(NovelPress._sysColor_back),
			NovelPress._getColor(NovelPress._sysColor_cur2)}));
		this.allBase2 = new LinearLayout(this);
		this.allBase2.setOrientation(LinearLayout.HORIZONTAL);
			//ライン
			int[] list = NPCan._getInstance().getCGList();
			this.lineBase = new LinearLayout[list.length];
			this.lineTop  = new TextView    [list.length];
			this.lineImg  = new View        [list.length];
			for(int i=0; i<list.length; i++) {
				//基盤
				this.lineBase[i] = new LinearLayout(this);
				this.lineBase[i].setOrientation(LinearLayout.VERTICAL);
				//番号
				this.lineTop[i] = new TextView(this);
				this.lineTop[i].setGravity(Gravity.CENTER);
				this.lineTop[i].setText(NovelPress.STR_NUMIND_CGLIST+FUtil._formatNum(i+1,2));
				this.lineTop[i].setTextColor(NovelPress._getColor(NovelPress._sysColor_font));
				ShapeDrawable lp = new ShapeDrawable(new RoundRectShape(new float[]{10,10,10,10,10,10,10,10},null,null));
				lp.getPaint().setColor(NovelPress._getColor(NovelPress._sysColor_cur));
				this.lineTop[i].setBackgroundDrawable(lp);
				//画像
				int imgNo = list[i];
				if(imgNo>=0) {
					this.lineImg[i] = new ImageView(this);
					((ImageView)this.lineImg[i]).setImageBitmap(NovelPress._loadImage("imageB/"+FUtil._formatNum(imgNo,2)));
					//リスナ
					this.lineBase[i].setId(imgNo);
					this.lineBase[i].setOnClickListener(this);
				} else {
					this.lineImg[i] = new View(this) {
						public void onDraw(Canvas can) {
							int size = Math.min(getWidth(),getHeight())/2;
							int x = getLeft()+(getWidth()-size)/2;
							int y = getTop()+(getHeight()-size)/2;
							Paint p = new Paint();
							p.setColor(NovelPress._getColor(NovelPress._sysColor_cur2));
							p.setStyle(Style.FILL);
							can.drawRoundRect(new RectF(x,y,x+size,y+size),5,5,p);
						}
					};
				}
			}
		//紐付ける
		for(int i=0; i<this.lineBase.length; i++) {
			this.lineBase[i].addView(this.lineImg[i]);
			this.lineBase[i].addView(this.lineTop[i]);
			this.allBase2.addView(this.lineBase[i]);
		}
		this.allBase.addView(this.allBase2);
		setContentView(this.allBase);
		//サイズを設定する
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		setSize(display.getWidth(),display.getHeight());
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//サイズを再設定する
		Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		setSize(display.getWidth(),display.getHeight());
	}
	private void setSize(int wW,int wH) {
		int lineH = wH-(AMenu._tabH+10);
		int lineTopH = ((int)new TextView(this).getTextSize())*2;
		this.allBase2.setLayoutParams(new FrameLayout.LayoutParams(wW,wH));
		for(int i=0; i<this.lineBase.length; i++) {
			if(this.lineImg[i] instanceof ImageView) {
				this.lineBase[i].setLayoutParams(new LinearLayout.LayoutParams(wW*9/10,lineH));
				this.lineTop[i] .setLayoutParams(new LinearLayout.LayoutParams(wW*9/10,lineTopH));
				this.lineImg[i] .setLayoutParams(new LinearLayout.LayoutParams(wW*9/10,lineH-lineTopH));
			} else {
				this.lineBase[i].setLayoutParams(new LinearLayout.LayoutParams(wW*3/10,lineH));
				this.lineTop[i] .setLayoutParams(new LinearLayout.LayoutParams(wW*3/10,lineTopH));
				this.lineImg[i] .setLayoutParams(new LinearLayout.LayoutParams(wW*3/10,lineH-lineTopH));
			}
			((LinearLayout.LayoutParams)this.lineBase[i].getLayoutParams()).setMargins(1,1,1,1);
		}
	}
	@Override
	public void onClick(View v) {
		Intent itt = new Intent(this,AFullDraw.class);
		itt.putExtra(NovelPress.ITTKEY_CGLIST_FULLDRAW,v.getId());
		startActivity(itt);
	}
}

