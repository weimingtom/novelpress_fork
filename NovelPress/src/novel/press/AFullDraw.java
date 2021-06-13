package novel.press;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class AFullDraw extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//フルスクリーン
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//画像番号を取得する
		int imgNo = getIntent().getExtras().getInt(NovelPress.ITTKEY_CGLIST_FULLDRAW);
		//画像ビューを背景に設定する
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(NovelPress._loadImage("imageB/"+FUtil._formatNum(imgNo,2)));
		iv.setBackgroundColor(NovelPress._getColor(NovelPress._sysColor_back,200));
		setContentView(iv);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
}
